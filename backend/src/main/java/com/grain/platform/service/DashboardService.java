package com.grain.platform.service;

import com.grain.platform.dto.dashboard.DashboardAlertItemResponse;
import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
import com.grain.platform.dto.dashboard.DashboardRecentSensorResponse;
import com.grain.platform.dto.dashboard.DashboardWarehouseHealthResponse;
import com.grain.platform.mapper.DashboardMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardOverviewResponse getOverview() {
        List<DashboardAlertItemResponse> alerts = dashboardMapper.selectLatestAlerts();
        List<DashboardRecentSensorResponse> recentSensorRecords = dashboardMapper.selectRecentSensorRecords();
        List<DashboardWarehouseHealthResponse> warehouseHealthList = dashboardMapper.selectWarehouseHealthList();
        return new DashboardOverviewResponse(
                dashboardMapper.countWarehouses(),
                dashboardMapper.countTodaySensorData(),
                alerts.size(),
                dashboardMapper.countArchivedPredictionCount(),
                alerts,
                recentSensorRecords,
                warehouseHealthList
        );
    }
}
