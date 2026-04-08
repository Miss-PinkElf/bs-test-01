package com.grain.platform.mapper;

import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SensorDataMapper {
    SensorData selectById(@Param("id") Long id);

    List<SensorDataPointDto> selectByCondition(@Param("warehouseId") Long warehouseId, @Param("metricCode") String metricCode);

    List<SensorDataPointDto> selectRecentByMetric(@Param("warehouseId") Long warehouseId, @Param("metricCode") String metricCode);

    void insert(SensorData sensorData);

    void update(SensorData sensorData);

    void deleteById(@Param("id") Long id);

    void insertBatch(@Param("list") List<SensorData> sensorDataList);
}
