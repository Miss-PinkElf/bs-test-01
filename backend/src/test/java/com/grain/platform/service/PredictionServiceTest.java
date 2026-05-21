package com.grain.platform.service;

import com.grain.platform.dto.prediction.PredictionPointDto;
import com.grain.platform.dto.prediction.PredictionRequest;
import com.grain.platform.entity.PredictionResult;
import com.grain.platform.entity.PredictionTask;
import com.grain.platform.entity.SensorMetric;
import com.grain.platform.entity.Warehouse;
import com.grain.platform.mapper.PredictionResultMapper;
import com.grain.platform.mapper.PredictionTaskMapper;
import com.grain.platform.mapper.WarehouseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private SensorDataService sensorDataService;

    @Mock
    private GrainTempService grainTempService;

    @Mock
    private ForecastService forecastService;

    @Mock
    private MetricService metricService;

    @Mock
    private PredictionTaskMapper predictionTaskMapper;

    @Mock
    private PredictionResultMapper predictionResultMapper;

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private PredictionService predictionService;

    @Test
    void predictUsesSelectedForecastStartTimeAndReplacesOldTasksInScope() {
        LocalDateTime actualEndTime = LocalDateTime.of(2026, 4, 25, 8, 40);
        LocalDateTime forecastStartTime = LocalDateTime.of(2026, 5, 1, 8, 40);
        LocalDateTime effectiveTrainEndTime = forecastStartTime.minusNanos(1);
        PredictionRequest request = new PredictionRequest(
                1L,
                "temperature",
                "AVG_TEMP",
                null,
                null,
                forecastStartTime,
                2
        );
        when(metricService.getMetric("temperature")).thenReturn(metric());
        when(warehouseMapper.selectById(1L)).thenReturn(warehouse());
        when(grainTempService.listPredictionSeries(1L, null, effectiveTrainEndTime, "AVG_TEMP")).thenReturn(List.of(
                new PredictionPointDto(LocalDateTime.of(2026, 4, 24, 8, 40), 20.0),
                new PredictionPointDto(actualEndTime, 21.0)
        ));
        when(forecastService.predictDaily(anyList(), eq(2), eq(forecastStartTime))).thenReturn(List.of(
                new PredictionPointDto(forecastStartTime, 22.0),
                new PredictionPointDto(forecastStartTime.plusDays(1), 23.0)
        ));
        when(predictionTaskMapper.selectIdsByScope(1L, "temperature", "AVG_TEMP")).thenReturn(List.of(8L, 7L));
        doAnswer(invocation -> {
            PredictionTask task = invocation.getArgument(0);
            task.setId(99L);
            return null;
        }).when(predictionTaskMapper).insert(any(PredictionTask.class));
        when(predictionResultMapper.selectByTaskId(99L)).thenReturn(List.of());
        when(grainTempService.listPredictionSeries(
                1L,
                LocalDateTime.of(2026, 4, 24, 8, 40),
                forecastStartTime.plusDays(1),
                "AVG_TEMP"
        )).thenReturn(List.of(
                new PredictionPointDto(LocalDateTime.of(2026, 4, 24, 8, 40), 20.0),
                new PredictionPointDto(actualEndTime, 21.0)
        ));

        var response = predictionService.predict(request, 2L);

        assertThat(response.forecastStartTime()).isEqualTo("2026-05-01 08:40:00");
        assertThat(response.forecastEndTime()).isEqualTo("2026-05-02 08:40:00");
        verify(forecastService).predictDaily(anyList(), eq(2), eq(forecastStartTime));

        InOrder deleteOrder = inOrder(predictionResultMapper, predictionTaskMapper);
        deleteOrder.verify(predictionResultMapper).deleteByTaskId(8L);
        deleteOrder.verify(predictionResultMapper).deleteByTaskId(7L);
        deleteOrder.verify(predictionTaskMapper).deleteById(8L);
        deleteOrder.verify(predictionTaskMapper).deleteById(7L);

        ArgumentCaptor<List<PredictionResult>> resultCaptor = ArgumentCaptor.forClass(List.class);
        verify(predictionResultMapper).insertBatch(resultCaptor.capture());
        assertThat(resultCaptor.getValue())
                .extracting(PredictionResult::getResultTime)
                .containsExactly(forecastStartTime, forecastStartTime.plusDays(1));
    }

    @Test
    void predictCapsTrainingSamplesBeforeSelectedForecastStartTime() {
        LocalDateTime historyTime = LocalDateTime.of(2026, 4, 24, 8, 40);
        LocalDateTime forecastStartTime = LocalDateTime.of(2026, 4, 25, 8, 40);
        LocalDateTime effectiveTrainEndTime = forecastStartTime.minusNanos(1);
        PredictionRequest request = new PredictionRequest(
                1L,
                "temperature",
                "AVG_TEMP",
                null,
                null,
                forecastStartTime,
                2
        );
        when(metricService.getMetric("temperature")).thenReturn(metric());
        when(warehouseMapper.selectById(1L)).thenReturn(warehouse());
        when(grainTempService.listPredictionSeries(1L, null, effectiveTrainEndTime, "AVG_TEMP")).thenReturn(List.of(
                new PredictionPointDto(historyTime, 20.0)
        ));
        when(forecastService.predictDaily(anyList(), eq(2), eq(forecastStartTime))).thenReturn(List.of(
                new PredictionPointDto(forecastStartTime, 20.0),
                new PredictionPointDto(forecastStartTime.plusDays(1), 20.0)
        ));
        when(predictionTaskMapper.selectIdsByScope(1L, "temperature", "AVG_TEMP")).thenReturn(List.of());
        doAnswer(invocation -> {
            PredictionTask task = invocation.getArgument(0);
            task.setId(100L);
            return null;
        }).when(predictionTaskMapper).insert(any(PredictionTask.class));
        when(predictionResultMapper.selectByTaskId(100L)).thenReturn(List.of());
        when(grainTempService.listPredictionSeries(1L, historyTime, forecastStartTime.plusDays(1), "AVG_TEMP")).thenReturn(List.of(
                new PredictionPointDto(historyTime, 20.0),
                new PredictionPointDto(forecastStartTime, 21.0)
        ));

        var response = predictionService.predict(request, 2L);

        assertThat(response.trainEndTime()).isEqualTo("2026-04-24 08:40:00");
        assertThat(response.forecastStartTime()).isEqualTo("2026-04-25 08:40:00");
        verify(grainTempService).listPredictionSeries(1L, null, effectiveTrainEndTime, "AVG_TEMP");
    }

    @Test
    void predictAlignsMidnightForecastStartTimeToActualSampleTime() {
        LocalDateTime historyTime = LocalDateTime.of(2026, 1, 12, 8, 40);
        LocalDateTime requestedForecastStartTime = LocalDateTime.of(2026, 1, 13, 0, 0);
        LocalDateTime alignedForecastStartTime = LocalDateTime.of(2026, 1, 13, 8, 40);
        PredictionRequest request = new PredictionRequest(
                1L,
                "temperature",
                "AVG_TEMP",
                null,
                null,
                requestedForecastStartTime,
                1
        );
        when(metricService.getMetric("temperature")).thenReturn(metric());
        when(warehouseMapper.selectById(1L)).thenReturn(warehouse());
        when(grainTempService.listPredictionSeries(
                1L,
                null,
                requestedForecastStartTime.minusNanos(1),
                "AVG_TEMP"
        )).thenReturn(List.of(new PredictionPointDto(historyTime, 16.6)));
        when(forecastService.predictDaily(anyList(), eq(1), eq(alignedForecastStartTime))).thenReturn(List.of(
                new PredictionPointDto(alignedForecastStartTime, 16.7)
        ));
        when(predictionTaskMapper.selectIdsByScope(1L, "temperature", "AVG_TEMP")).thenReturn(List.of());
        doAnswer(invocation -> {
            PredictionTask task = invocation.getArgument(0);
            task.setId(101L);
            return null;
        }).when(predictionTaskMapper).insert(any(PredictionTask.class));
        when(predictionResultMapper.selectByTaskId(101L)).thenReturn(List.of());
        when(grainTempService.listPredictionSeries(1L, historyTime, alignedForecastStartTime, "AVG_TEMP")).thenReturn(List.of(
                new PredictionPointDto(historyTime, 16.6),
                new PredictionPointDto(alignedForecastStartTime, 16.8)
        ));

        var response = predictionService.predict(request, 2L);

        assertThat(response.forecastStartTime()).isEqualTo("2026-01-13 08:40:00");
        verify(forecastService).predictDaily(anyList(), eq(1), eq(alignedForecastStartTime));
    }

    private SensorMetric metric() {
        SensorMetric metric = new SensorMetric();
        metric.setMetricCode("temperature");
        metric.setMetricName("温度");
        metric.setUnit("°C");
        metric.setMaxThreshold(BigDecimal.valueOf(28));
        return metric;
    }

    private Warehouse warehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setWarehouseName("一号平房仓");
        return warehouse;
    }
}
