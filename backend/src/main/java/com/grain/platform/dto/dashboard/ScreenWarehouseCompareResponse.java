package com.grain.platform.dto.dashboard;

public record ScreenWarehouseCompareResponse(
        Long warehouseId,
        String warehouseName,
        Integer healthScore,
        Double avgTemp,
        Double latestForecastValue,
        String riskLevel
) {
}
