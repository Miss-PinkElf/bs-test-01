package com.grain.platform.service;

import com.grain.platform.dto.dashboard.DashboardAlertItemResponse;
import com.grain.platform.dto.dashboard.DashboardLatestSummaryResponse;
import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
import com.grain.platform.dto.dashboard.DashboardWarehouseHealthResponse;
import com.grain.platform.mapper.DashboardMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardOverviewResponse getOverview() {
        List<DashboardAlertItemResponse> alerts = Stream.concat(
                        dashboardMapper.selectLatestRealAlerts().stream(),
                        dashboardMapper.selectLatestPredictionAlerts().stream()
                )
                .sorted(Comparator.comparing(DashboardAlertItemResponse::eventTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .toList();
        List<DashboardLatestSummaryResponse> latestGrainSummaries = dashboardMapper.selectLatestGrainSummaries();
        List<DashboardWarehouseHealthResponse> warehouseHealthList = dashboardMapper.selectWarehouseHealthList();
        return new DashboardOverviewResponse(
                dashboardMapper.countWarehouses(),
                dashboardMapper.countGrainTempSummaryCount(),
                dashboardMapper.countLatestRealAlertCount(),
                dashboardMapper.countLatestPredictionAlertCount(),
                dashboardMapper.countArchivedPredictionCount(),
                alerts,
                latestGrainSummaries,
                warehouseHealthList
        );
    }
}
