package com.grain.platform.dto.dashboard;

public record ScreenPredictionTaskItemResponse(
        Long taskId,
        String taskNo,
        Long warehouseId,
        String warehouseName,
        String riskLevel,
        String requestedAt,
        Integer forecastDays,
        String summary
) {
}
