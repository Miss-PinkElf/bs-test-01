package com.grain.platform.service;

import com.grain.platform.common.PageResult;
import com.grain.platform.dto.prediction.PredictionPointDto;
import com.grain.platform.dto.prediction.PredictionRequest;
import com.grain.platform.dto.prediction.PredictionResultItemDto;
import com.grain.platform.dto.prediction.PredictionTaskResponse;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.entity.PredictionResult;
import com.grain.platform.entity.PredictionTask;
import com.grain.platform.entity.SensorMetric;
import com.grain.platform.entity.Warehouse;
import com.grain.platform.mapper.PredictionResultMapper;
import com.grain.platform.mapper.PredictionTaskMapper;
import com.grain.platform.mapper.WarehouseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

@Service
// 负责把真实历史序列转换成“可归档、可回放、可展示”的预测任务和结果时间线。
public class PredictionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SensorDataService sensorDataService;
    private final GrainTempService grainTempService;
    private final ForecastService forecastService;
    private final MetricService metricService;
    private final PredictionTaskMapper predictionTaskMapper;
    private final PredictionResultMapper predictionResultMapper;
    private final WarehouseMapper warehouseMapper;

    public PredictionService(SensorDataService sensorDataService,
                             GrainTempService grainTempService,
                             ForecastService forecastService,
                             MetricService metricService,
                             PredictionTaskMapper predictionTaskMapper,
                             PredictionResultMapper predictionResultMapper,
                             WarehouseMapper warehouseMapper) {
        this.sensorDataService = sensorDataService;
        this.grainTempService = grainTempService;
        this.forecastService = forecastService;
        this.metricService = metricService;
        this.predictionTaskMapper = predictionTaskMapper;
        this.predictionResultMapper = predictionResultMapper;
        this.warehouseMapper = warehouseMapper;
    }

    @Transactional
    public PredictionTaskResponse predict(PredictionRequest request, Long operatorUserId) {
        SensorMetric metric = metricService.getMetric(request.metricCode());
        Warehouse warehouse = requireWarehouse(request.warehouseId());
        // 温度指标需要先把目标类型收口成预测主线，环境指标则固定走传感器时序。
        String targetType = normalizeTargetType(request.targetType(), request.metricCode());
        String dataSourceType = "temperature".equals(request.metricCode()) ? "GRAIN_TEMP_SUMMARY" : "SENSOR_DATA";
        List<PredictionPointDto> actualSeries = loadActualSeries(request, targetType);
        if (actualSeries.isEmpty()) {
            throw new IllegalArgumentException("可用于预测的真实历史数据不足");
        }

        // 预测算法要求时间升序输入，因此先把历史样本统一排好序。
        actualSeries = actualSeries.stream().sorted(Comparator.comparing(PredictionPointDto::time)).toList();
        LocalDateTime forecastStartTime = resolveEffectiveForecastStartTime(request, actualSeries);
        List<PredictionPointDto> forecastSeries = forecastService.predictDaily(
                actualSeries,
                request.forecastDays(),
                forecastStartTime
        );

        LocalDateTime now = LocalDateTime.now();
        deleteExistingTasksInScope(request.warehouseId(), request.metricCode(), targetType);
        PredictionTask task = new PredictionTask();
        task.setTaskNo("TASK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        task.setParentTaskId(null);
        task.setTaskRound(1);
        task.setWarehouseId(request.warehouseId());
        task.setMetricCode(request.metricCode());
        task.setDataSourceType(dataSourceType);
        task.setTargetType(targetType);
        task.setAlgorithmCode("LINEAR_REGRESSION");
        task.setAlgorithmName("线性回归");
        task.setTrainStartTime(actualSeries.get(0).time());
        task.setTrainEndTime(actualSeries.get(actualSeries.size() - 1).time());
        task.setForecastStartTime(forecastSeries.isEmpty() ? actualSeries.get(actualSeries.size() - 1).time() : forecastSeries.get(0).time());
        task.setForecastEndTime(forecastSeries.isEmpty() ? actualSeries.get(actualSeries.size() - 1).time() : forecastSeries.get(forecastSeries.size() - 1).time());
        task.setBasedOnActualEndTime(actualSeries.get(actualSeries.size() - 1).time());
        task.setForecastDays(request.forecastDays());
        task.setSampleSize(actualSeries.size());
        task.setTriggerType("INITIAL");
        task.setAdjustStatus("UNADJUSTED");
        task.setStatus("SUCCESS");
        task.setRiskLevel(calcRiskLevel(forecastSeries, metric));
        task.setRequestedBy(operatorUserId);
        task.setRequestedAt(now);
        task.setCompletedAt(now);
        task.setSummary(buildSummary(warehouse.getWarehouseName(), targetType, request.forecastDays()));
        task.setRemark("本期 MVP：保留修正扩展字段，不实现修正入口");
        predictionTaskMapper.insert(task);

        List<PredictionResult> resultEntities = buildForecastEntities(task.getId(), forecastSeries, metric);
        if (!resultEntities.isEmpty()) {
            predictionResultMapper.insertBatch(resultEntities);
        }

        return toResponse(task, warehouse, metric, loadDisplayActualSeries(task), predictionResultMapper.selectByTaskId(task.getId()));
    }

    public List<PredictionTaskResponse> listTasks(Long warehouseScope) {
        if (warehouseScope != null) {
            return predictionTaskMapper.selectAll().stream()
                    .filter(task -> Objects.equals(task.getWarehouseId(), warehouseScope))
                    .map(this::toTaskResponse)
                    .toList();
        }
        return predictionTaskMapper.selectAll().stream()
                .map(this::toTaskResponse)
                .toList();
    }

    public PageResult<PredictionTaskResponse> listTaskPage(String keyword, Integer pageNum, Integer pageSize, Long warehouseScope) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        int finalPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int finalPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        if (warehouseScope != null) {
            List<PredictionTaskResponse> filtered = predictionTaskMapper.selectAll().stream()
                    .filter(task -> Objects.equals(task.getWarehouseId(), warehouseScope))
                    .map(this::toTaskResponse)
                    .filter(task -> matchesTaskKeyword(task, kw))
                    .toList();
            return paginateTaskResponses(filtered, finalPageNum, finalPageSize);
        }
        long total = predictionTaskMapper.countPage(kw);
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        // 预测分页只裁 task 主表，但返回仍组装完整任务详情，前端才能直接切换摘要和图表。
        List<PredictionTaskResponse> list = predictionTaskMapper.selectPage(kw, offset, finalPageSize).stream()
                .map(this::toTaskResponse)
                .toList();
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }

    public PredictionTaskResponse getTask(Long taskId) {
        PredictionTask task = predictionTaskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("预测任务不存在");
        }
        return toTaskResponse(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        PredictionTask task = predictionTaskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("预测任务不存在");
        }
        predictionResultMapper.deleteByTaskId(taskId);
        predictionTaskMapper.deleteById(taskId);
    }

    @Transactional
    public void deleteTasksBatch(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            throw new IllegalArgumentException("请选择要删除的预测任务");
        }
        LinkedHashSet<Long> distinct = new LinkedHashSet<>(taskIds);
        // 批量删除先做一轮全集校验，避免删到一半才发现脏 id 导致结果不一致。
        for (Long id : distinct) {
            PredictionTask task = predictionTaskMapper.selectById(id);
            if (task == null) {
                throw new IllegalArgumentException("预测任务不存在: " + id);
            }
        }
        for (Long id : distinct) {
            predictionResultMapper.deleteByTaskId(id);
            predictionTaskMapper.deleteById(id);
        }
    }

    private PredictionTaskResponse toTaskResponse(PredictionTask task) {
        return toResponse(
                task,
                requireWarehouse(task.getWarehouseId()),
                metricService.getMetric(task.getMetricCode()),
                loadDisplayActualSeries(task),
                predictionResultMapper.selectByTaskId(task.getId())
        );
    }

    private boolean matchesTaskKeyword(PredictionTaskResponse task, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        String haystack = String.join("|",
                valueOf(task.taskNo()),
                valueOf(task.warehouseName()),
                valueOf(task.riskLevel()),
                valueOf(task.forecastDays()),
                valueOf(task.forecastStartTime()),
                valueOf(task.forecastEndTime()),
                valueOf(task.requestedAt()),
                valueOf(task.summary()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(normalized);
    }

    private PageResult<PredictionTaskResponse> paginateTaskResponses(List<PredictionTaskResponse> tasks,
                                                                     int pageNum,
                                                                     int pageSize) {
        long total = tasks.size();
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        int finalPageNum = Math.min(pageNum, maxPage);
        int fromIndex = (finalPageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, tasks.size());
        List<PredictionTaskResponse> list = fromIndex >= tasks.size()
                ? List.of()
                : tasks.subList(fromIndex, toIndex);
        return new PageResult<>(list, finalPageNum, pageSize, total);
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private List<PredictionPointDto> loadActualSeries(PredictionRequest request, String targetType) {
        LocalDateTime trainEndTime = resolveEffectiveTrainEndTime(request);
        if ("temperature".equals(request.metricCode())) {
            return grainTempService.listPredictionSeries(
                    request.warehouseId(),
                    request.trainStartTime(),
                    trainEndTime,
                    targetType
            );
        }
        List<SensorDataPointDto> history = sensorDataService.list(request.warehouseId(), request.metricCode());
        return history.stream()
                .filter(item -> request.trainStartTime() == null || !item.getCollectedAt().isBefore(request.trainStartTime()))
                .filter(item -> trainEndTime == null || !item.getCollectedAt().isAfter(trainEndTime))
                .map(item -> new PredictionPointDto(item.getCollectedAt(), item.getMetricValue()))
                .toList();
    }

    private LocalDateTime resolveEffectiveTrainEndTime(PredictionRequest request) {
        LocalDateTime forecastStartTime = request.forecastStartTime();
        if (forecastStartTime == null) {
            return request.trainEndTime();
        }

        LocalDateTime latestAllowedTrainEndTime = forecastStartTime.minusNanos(1);
        if (request.trainEndTime() == null || !request.trainEndTime().isBefore(forecastStartTime)) {
            return latestAllowedTrainEndTime;
        }
        return request.trainEndTime();
    }

    private LocalDateTime resolveEffectiveForecastStartTime(PredictionRequest request,
                                                            List<PredictionPointDto> actualSeries) {
        LocalDateTime forecastStartTime = request.forecastStartTime();
        if (forecastStartTime == null || actualSeries.isEmpty()) {
            return forecastStartTime;
        }

        if (!LocalTime.MIDNIGHT.equals(forecastStartTime.toLocalTime())) {
            return forecastStartTime;
        }

        LocalDateTime lastActualTime = actualSeries.get(actualSeries.size() - 1).time();
        return forecastStartTime.toLocalDate().atTime(lastActualTime.toLocalTime());
    }

    private List<PredictionPointDto> loadDisplayActualSeries(PredictionTask task) {
        LocalDateTime displayEndTime = task.getForecastEndTime() == null
                ? task.getTrainEndTime()
                : task.getForecastEndTime();
        // 任务详情展示允许把预测区间内后来录入的真实值回填回来，但不改变任务当时的训练样本。
        if ("temperature".equals(task.getMetricCode())) {
            return grainTempService.listPredictionSeries(
                    task.getWarehouseId(),
                    task.getTrainStartTime(),
                    displayEndTime,
                    task.getTargetType()
            );
        }
        return sensorDataService.list(task.getWarehouseId(), task.getMetricCode()).stream()
                .filter(item -> task.getTrainStartTime() == null || !item.getCollectedAt().isBefore(task.getTrainStartTime()))
                .filter(item -> displayEndTime == null || !item.getCollectedAt().isAfter(displayEndTime))
                .map(item -> new PredictionPointDto(item.getCollectedAt(), item.getMetricValue()))
                .toList();
    }

    private void deleteExistingTasksInScope(Long warehouseId, String metricCode, String targetType) {
        List<Long> taskIds = predictionTaskMapper.selectIdsByScope(warehouseId, metricCode, targetType);
        for (Long taskId : taskIds) {
            predictionResultMapper.deleteByTaskId(taskId);
        }
        for (Long taskId : taskIds) {
            predictionTaskMapper.deleteById(taskId);
        }
    }

    private List<PredictionResult> buildForecastEntities(Long taskId,
                                                         List<PredictionPointDto> forecastSeries,
                                                         SensorMetric metric) {
        List<PredictionResult> list = new ArrayList<>();
        for (int index = 0; index < forecastSeries.size(); index++) {
            PredictionPointDto item = forecastSeries.get(index);
            WarningInfo warningInfo = resolveWarning(item.value(), metric);
            PredictionResult entity = new PredictionResult();
            entity.setTaskId(taskId);
            entity.setPhaseType("FUTURE");
            entity.setStepIndex(index + 1);
            entity.setResultTime(item.time());
            entity.setActualValue(null);
            entity.setPredictedValue(BigDecimal.valueOf(item.value()).setScale(2, RoundingMode.HALF_UP));
            entity.setErrorValue(null);
            entity.setErrorRate(null);
            entity.setWarningLevel(warningInfo.level());
            entity.setWarningFlag(warningInfo.flag());
            entity.setWarningMessage(warningInfo.message());
            entity.setIsCorrected(false);
            entity.setRemark("数据库优先 MVP 自动生成的未来预测结果");
            list.add(entity);
        }
        return list;
    }

    private PredictionTaskResponse toResponse(PredictionTask task,
                                              Warehouse warehouse,
                                              SensorMetric metric,
                                              List<PredictionPointDto> actualSeries,
                                              List<PredictionResult> forecastResults) {
        // 前端只消费一个 resultList：这里先把真实值和预测值并到同一时间轴上。
        List<PredictionResultItemDto> merged = mergeTimeline(actualSeries, forecastResults);

        return new PredictionTaskResponse(
                task.getId(),
                task.getTaskNo(),
                task.getParentTaskId(),
                task.getTaskRound(),
                task.getWarehouseId(),
                warehouse.getWarehouseName(),
                task.getMetricCode(),
                metric.getMetricName(),
                metric.getUnit(),
                metric.getMaxThreshold() == null ? null : metric.getMaxThreshold().doubleValue(),
                task.getTargetType(),
                task.getDataSourceType(),
                task.getAlgorithmCode(),
                task.getAlgorithmName(),
                format(task.getTrainStartTime()),
                format(task.getTrainEndTime()),
                format(task.getForecastStartTime()),
                format(task.getForecastEndTime()),
                format(task.getBasedOnActualEndTime()),
                task.getForecastDays(),
                task.getTriggerType(),
                task.getAdjustStatus(),
                task.getRiskLevel(),
                format(task.getRequestedAt()),
                format(task.getCompletedAt()),
                task.getSummary(),
                merged
        );
    }

    private List<PredictionResultItemDto> mergeTimeline(List<PredictionPointDto> actualSeries,
                                                        List<PredictionResult> forecastResults) {
        TreeMap<LocalDateTime, TimelineEntry> timeline = new TreeMap<>();
        int actualStep = 0;

        for (PredictionPointDto item : actualSeries.stream()
                .sorted(Comparator.comparing(PredictionPointDto::time))
                .toList()) {
            TimelineEntry entry = timeline.computeIfAbsent(item.time(), TimelineEntry::new);
            entry.setActualValue(item.value());
            entry.setActualStepIndex(++actualStep);
        }

        for (PredictionResult item : forecastResults.stream()
                .sorted(Comparator.comparing(PredictionResult::getResultTime).thenComparing(PredictionResult::getStepIndex))
                .toList()) {
            TimelineEntry entry = timeline.computeIfAbsent(item.getResultTime(), TimelineEntry::new);
            entry.setPredictionResult(item);
        }

        // TreeMap 保证结果天然按时间排序，前端可以直接画表格和折线图。
        return timeline.values().stream()
                .map(this::toTimelineItem)
                .toList();
    }

    private PredictionResultItemDto toTimelineItem(TimelineEntry entry) {
        PredictionResult forecast = entry.getPredictionResult();
        return new PredictionResultItemDto(
                forecast == null ? null : forecast.getId(),
                forecast == null ? "ACTUAL" : forecast.getPhaseType(),
                forecast == null ? entry.getActualStepIndex() : forecast.getStepIndex(),
                format(entry.getTime()),
                entry.getActualValue(),
                forecast == null || forecast.getPredictedValue() == null ? null : forecast.getPredictedValue().doubleValue(),
                forecast == null || forecast.getErrorValue() == null ? null : forecast.getErrorValue().doubleValue(),
                forecast == null || forecast.getErrorRate() == null ? null : forecast.getErrorRate().doubleValue(),
                forecast == null ? null : forecast.getWarningLevel(),
                forecast != null && Boolean.TRUE.equals(forecast.getWarningFlag()),
                forecast == null ? null : forecast.getWarningMessage(),
                forecast != null && Boolean.TRUE.equals(forecast.getIsCorrected())
        );
    }

    private String buildSummary(String warehouseName, String targetType, int forecastDays) {
        String targetLabel = switch (targetType) {
            case "LAYER_1_AVG" -> "第一层平均温度";
            case "LAYER_2_AVG" -> "第二层平均温度";
            case "LAYER_3_AVG" -> "第三层平均温度";
            case "LAYER_4_AVG" -> "第四层平均温度";
            default -> "整仓平均温度";
        };
        return warehouseName + " 基于历史真实数据完成 " + targetLabel + " 的未来 " + forecastDays + " 天预测";
    }

    private String normalizeTargetType(String targetType, String metricCode) {
        if (!"temperature".equals(metricCode)) {
            return "METRIC_VALUE";
        }
        return (targetType == null || targetType.isBlank()) ? "AVG_TEMP" : targetType;
    }

    private String calcRiskLevel(List<PredictionPointDto> points, SensorMetric metric) {
        double peak = points.stream().mapToDouble(PredictionPointDto::value).max().orElse(0.0);
        if (metric.getMaxThreshold() == null) {
            return "NORMAL";
        }
        double warningThreshold = metric.getMaxThreshold().doubleValue();
        if (peak >= warningThreshold) {
            return "WARNING";
        }
        if (peak >= warningThreshold * 0.9) {
            return "ATTENTION";
        }
        return "NORMAL";
    }

    private WarningInfo resolveWarning(Double value, SensorMetric metric) {
        if (metric.getMaxThreshold() == null || value == null) {
            return new WarningInfo("NORMAL", false, null);
        }
        double threshold = metric.getMaxThreshold().doubleValue();
        if (value >= threshold) {
            return new WarningInfo("WARNING", true, "预计该时间点超过高温阈值");
        }
        if (value >= threshold * 0.9) {
            return new WarningInfo("ATTENTION", true, "预计该时间点接近高温阈值");
        }
        return new WarningInfo("NORMAL", false, null);
    }

    private Warehouse requireWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库不存在");
        }
        return warehouse;
    }

    private String format(LocalDateTime time) {
        return time == null ? null : time.format(FORMATTER);
    }

    private record WarningInfo(String level, boolean flag, String message) {
    }

    private static final class TimelineEntry {
        private final LocalDateTime time;
        private Integer actualStepIndex;
        private Double actualValue;
        private PredictionResult predictionResult;

        private TimelineEntry(LocalDateTime time) {
            this.time = time;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public Integer getActualStepIndex() {
            return actualStepIndex;
        }

        public void setActualStepIndex(Integer actualStepIndex) {
            this.actualStepIndex = actualStepIndex;
        }

        public Double getActualValue() {
            return actualValue;
        }

        public void setActualValue(Double actualValue) {
            this.actualValue = actualValue;
        }

        public PredictionResult getPredictionResult() {
            return predictionResult;
        }

        public void setPredictionResult(PredictionResult predictionResult) {
            this.predictionResult = predictionResult;
        }
    }
}


