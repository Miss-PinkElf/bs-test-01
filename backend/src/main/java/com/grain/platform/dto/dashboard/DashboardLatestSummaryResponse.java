package com.grain.platform.dto.dashboard;

public record DashboardLatestSummaryResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        Double avgTemp,
        Double maxTemp,
        Double minTemp,
        String collectedAt,
        String warningLevel,
        Boolean warningFlag,
        String warningMessage
) {
}
