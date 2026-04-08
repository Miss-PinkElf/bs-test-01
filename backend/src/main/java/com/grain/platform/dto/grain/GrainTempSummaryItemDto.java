package com.grain.platform.dto.grain;

public record GrainTempSummaryItemDto(
        Long id,
        Long warehouseId,
        String warehouseName,
        String collectedAt,
        Double avgTemp,
        Double maxTemp,
        Double minTemp,
        Double layer1Avg,
        Double layer2Avg,
        Double layer3Avg,
        Double layer4Avg,
        String warningLevel,
        Boolean warningFlag,
        String warningMessage,
        String analysisResult
) {
}
