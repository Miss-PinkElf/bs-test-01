package com.grain.platform.mapper;

import com.grain.platform.dto.sensor.MetricOptionResponse;
import com.grain.platform.entity.SensorMetric;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SensorMetricMapper {
    List<MetricOptionResponse> selectActiveOptions();

    SensorMetric selectByCode(@Param("metricCode") String metricCode);
}
