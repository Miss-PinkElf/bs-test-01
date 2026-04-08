package com.grain.platform.dto.dashboard;

public record DashboardAlertItemResponse(
        String title,
        String level,
        String sourceType,
        String warehouseName,
        String eventTime,
        String description
) {
}
