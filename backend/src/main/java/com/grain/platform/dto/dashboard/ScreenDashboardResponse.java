package com.grain.platform.dto.dashboard;

import java.util.List;

public record ScreenDashboardResponse(
        DashboardOverviewResponse overview,
        List<ScreenTrendPointResponse> grainTrend,
        List<ScreenTrendPointResponse> alertTrend,
        List<ScreenTrendPointResponse> predictionTrend,
        List<ScreenWarehouseCompareResponse> warehouseComparison,
        List<ScreenPredictionTaskItemResponse> latestPredictionTasks
) {
}
