package com.grain.platform.service;

import com.grain.platform.dto.prediction.PredictionPointDto;
import com.grain.platform.dto.prediction.PredictionRequest;
import com.grain.platform.dto.prediction.PredictionResultItemDto;
import com.grain.platform.dto.prediction.PredictionTaskResponse;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.entity.PredictionResult;
import com.grain.platform.entity.PredictionTask;
import com.grain.platform.entity.SensorMetric;
import com.grain.platform.mapper.PredictionResultMapper;
import com.grain.platform.mapper.PredictionTaskMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PredictionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SensorDataService sensorDataService;
    private final ForecastService forecastService;
    private final MetricService metricService;
    private final PredictionTaskMapper predictionTaskMapper;
    private final PredictionResultMapper predictionResultMapper;

    public PredictionService(
            SensorDataService sensorDataService,
            ForecastService forecastService,
            MetricService metricService,
            PredictionTaskMapper predictionTaskMapper,
            PredictionResultMapper predictionResultMapper
    ) {
        this.sensorDataService = sensorDataService;
        this.forecastService = forecastService;
        this.metricService = metricService;
        this.predictionTaskMapper = predictionTaskMapper;
        this.predictionResultMapper = predictionResultMapper;
    }

    public PredictionTaskResponse predict(PredictionRequest request) {
        SensorMetric metric = metricService.getMetric(request.metricCode());
        List<SensorDataPointDto> history = sensorDataService.list(request.warehouseId(), request.metricCode());
        List<PredictionPointDto> points = forecastService.predict(history, request.futureSteps());

        LocalDateTime now = LocalDateTime.now();
        PredictionTask task = new PredictionTask(
                null,
                "TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                request.warehouseId(),
                request.metricCode(),
                "LINEAR_REGRESSION",
                "线性回归",
                request.futureSteps(),
                history.size(),
                "SUCCESS",
                calcRiskLevel(points, metric),
                1L,
                now,
                now,
                history.isEmpty() ? "样本为空，仅返回空结果" : "预测执行完成",
                null,
                null
        );
        predictionTaskMapper.insert(task);

        List<PredictionResult> resultEntities = buildResultEntities(task.getId(), points);
        if (!resultEntities.isEmpty()) {
            predictionResultMapper.insertBatch(resultEntities);
        }

        return toResponse(task, metric, predictionResultMapper.selectByTaskId(task.getId()));
    }

    public List<PredictionTaskResponse> listTasks() {
        return predictionTaskMapper.selectAll().stream()
                .map(task -> toResponse(task, metricService.getMetric(task.getMetricCode()), predictionResultMapper.selectByTaskId(task.getId())))
                .toList();
    }

    public PredictionTaskResponse getTask(Long taskId) {
        PredictionTask task = predictionTaskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("预测任务不存在");
        }
        return toResponse(task, metricService.getMetric(task.getMetricCode()), predictionResultMapper.selectByTaskId(taskId));
    }

    private List<PredictionResult> buildResultEntities(Long taskId, List<PredictionPointDto> points) {
        int[] step = {0};
        return points.stream()
                .filter(item -> item.predictedValue() != null)
                .map(item -> new PredictionResult(
                        null,
                        taskId,
                        ++step[0],
                        item.time(),
                        item.actualValue() == null ? null : BigDecimal.valueOf(item.actualValue()),
                        BigDecimal.valueOf(item.predictedValue()),
                        null
                ))
                .toList();
    }

    private PredictionTaskResponse toResponse(PredictionTask task, SensorMetric metric, List<PredictionResult> results) {
        return new PredictionTaskResponse(
                task.getId(),
                task.getTaskNo(),
                task.getMetricCode(),
                metric.getMetricName(),
                metric.getUnit(),
                metric.getMaxThreshold() == null ? null : metric.getMaxThreshold().doubleValue(),
                task.getAlgorithmName(),
                task.getRiskLevel(),
                task.getRequestedAt() == null ? null : task.getRequestedAt().format(FORMATTER),
                task.getSummary(),
                results.stream().map(item -> new PredictionResultItemDto(
                        item.getStepIndex(),
                        item.getPredictedTime() == null ? null : item.getPredictedTime().format(FORMATTER),
                        item.getActualValue() == null ? null : item.getActualValue().doubleValue(),
                        item.getPredictedValue() == null ? null : item.getPredictedValue().doubleValue()
                )).toList()
        );
    }

    private String calcRiskLevel(List<PredictionPointDto> points, SensorMetric metric) {
        double peak = points.stream()
                .filter(item -> item.predictedValue() != null)
                .mapToDouble(PredictionPointDto::predictedValue)
                .max()
                .orElse(0.0);

        BigDecimal maxThreshold = metric.getMaxThreshold();
        if (maxThreshold == null) {
            return "NORMAL";
        }

        double warningThreshold = maxThreshold.doubleValue();
        double attentionThreshold = warningThreshold * 0.9;

        if (peak >= warningThreshold) {
            return "WARNING";
        }
        if (peak >= attentionThreshold) {
            return "ATTENTION";
        }
        return "NORMAL";
    }
}
