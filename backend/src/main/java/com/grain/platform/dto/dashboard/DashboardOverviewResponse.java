package com.grain.platform.dto.dashboard;

import java.util.List;

public record DashboardOverviewResponse(
        int warehouseCount,
        int todayDataCount,
        int alertCount,
        List<String> latestAlerts
) {
}
