package com.grain.platform.service;

import com.grain.platform.dto.sensor.SensorDataCreateRequest;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.dto.sensor.SensorTrendPointDto;
import com.grain.platform.dto.sensor.SensorTrendResponse;
import com.grain.platform.entity.SensorData;
import com.grain.platform.mapper.SensorDataMapper;
import com.grain.platform.vo.common.IdVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SensorDataService {

    private final SensorDataMapper sensorDataMapper;

    public SensorDataService(SensorDataMapper sensorDataMapper) {
        this.sensorDataMapper = sensorDataMapper;
    }

    public List<SensorDataPointDto> list(Long warehouseId, String metricCode) {
        return sensorDataMapper.selectByCondition(warehouseId, metricCode);
    }

    public SensorTrendResponse trend(Long warehouseId, String metricCode) {
        List<SensorTrendPointDto> points = sensorDataMapper.selectByCondition(warehouseId, metricCode)
                .stream()
                .map(item -> new SensorTrendPointDto(item.getCollectedAt(), item.getMetricValue()))
                .toList();
        return new SensorTrendResponse(points);
    }

    public IdVO create(SensorDataCreateRequest request) {
        SensorData sensorData = new SensorData(
                null,
                request.warehouseId(),
                request.metricCode(),
                BigDecimal.valueOf(request.metricValue()),
                request.collectedAt() == null ? LocalDateTime.now() : request.collectedAt(),
                "MANUAL",
                null,
                "NORMAL",
                null,
                1L,
                null,
                null
        );
        sensorDataMapper.insert(sensorData);
        return new IdVO(sensorData.getId());
    }

    public List<SensorDataPointDto> listRecentTemperatureHistory(Long warehouseId) {
        return sensorDataMapper.selectRecentByMetric(warehouseId, "temperature");
    }
}
