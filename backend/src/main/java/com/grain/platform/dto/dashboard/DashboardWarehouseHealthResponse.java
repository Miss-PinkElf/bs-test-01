package com.grain.platform.dto.dashboard;

public record DashboardWarehouseHealthResponse(
        Long warehouseId,
        String warehouseName,
        Integer healthScore,
        String riskLevel
) {
}
