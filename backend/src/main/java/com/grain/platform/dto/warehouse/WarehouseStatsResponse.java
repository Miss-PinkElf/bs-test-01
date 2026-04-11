package com.grain.platform.dto.warehouse;

public record WarehouseStatsResponse(
        long totalCount,
        long activeCount,
        long nonActiveCount
) {
}
