package com.grain.platform.mapper;

import com.grain.platform.dto.dashboard.DashboardAlertItemResponse;
import com.grain.platform.dto.dashboard.DashboardRecentSensorResponse;
import com.grain.platform.dto.dashboard.DashboardWarehouseHealthResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DashboardMapper {
    int countWarehouses();

    int countTodaySensorData();

    int countArchivedPredictionCount();

    List<DashboardAlertItemResponse> selectLatestAlerts();

    List<DashboardRecentSensorResponse> selectRecentSensorRecords();

    List<DashboardWarehouseHealthResponse> selectWarehouseHealthList();
}
