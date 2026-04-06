package com.grain.platform.service;

import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
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
        List<String> alerts = dashboardMapper.selectLatestAlerts();
        return new DashboardOverviewResponse(
                dashboardMapper.countWarehouses(),
                dashboardMapper.countTodaySensorData(),
                alerts.size(),
                dashboardMapper.countArchivedPredictionCount(),
                alerts
        );
    }
}
