package com.grain.platform.dto.grain;

public record GrainTempImportResultDto(
        String batchNo,
        Long warehouseId,
        String warehouseName,
        String collectedAt,
        int pointCount,
        boolean summaryGenerated,
        String warningLevel,
        String warningMessage
) {
}
