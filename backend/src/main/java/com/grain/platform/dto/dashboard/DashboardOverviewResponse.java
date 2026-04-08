package com.grain.platform.dto.dashboard;

import java.util.List;

public record DashboardOverviewResponse(
        int warehouseCount,
        int grainSummaryCount,
        int realAlertCount,
        int predictionAlertCount,
        int archivedPredictionCount,
        List<DashboardAlertItemResponse> latestAlerts,
        List<DashboardLatestSummaryResponse> latestGrainSummaries,
        List<DashboardWarehouseHealthResponse> warehouseHealthList
) {
}
