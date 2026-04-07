package com.grain.platform.dto.dashboard;

public record DashboardRecentSensorResponse(
        Long id,
        String warehouseName,
        String metricName,
        Double metricValue,
        String collectedAt,
        String qualityFlag
) {
}
