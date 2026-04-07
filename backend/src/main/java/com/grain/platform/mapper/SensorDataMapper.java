package com.grain.platform.mapper;

import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SensorDataMapper {
    List<SensorDataPointDto> selectByCondition(@Param("warehouseId") Long warehouseId, @Param("metricCode") String metricCode);

    List<SensorDataPointDto> selectRecentByMetric(@Param("warehouseId") Long warehouseId, @Param("metricCode") String metricCode);

    void insert(SensorData sensorData);

    void insertBatch(@Param("list") List<SensorData> sensorDataList);
}
