package com.grain.platform.service;

import com.grain.platform.dto.sensor.MetricOptionResponse;
import com.grain.platform.entity.SensorMetric;
import com.grain.platform.mapper.SensorMetricMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricService {

    private final SensorMetricMapper sensorMetricMapper;

    public MetricService(SensorMetricMapper sensorMetricMapper) {
        this.sensorMetricMapper = sensorMetricMapper;
    }

    public List<MetricOptionResponse> listActiveOptions() {
        return sensorMetricMapper.selectActiveOptions();
    }

    public SensorMetric getMetric(String metricCode) {
        SensorMetric metric = sensorMetricMapper.selectByCode(metricCode);
        if (metric == null) {
            throw new IllegalArgumentException("指标不存在");
        }
        return metric;
    }
}
