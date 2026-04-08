package com.grain.platform.dto.grain;

public record GrainTempRecordItemDto(
        Long id,
        Long warehouseId,
        String warehouseName,
        Long pointId,
        String pointName,
        String zoneCode,
        Integer layerNo,
        Integer pointNo,
        String collectedAt,
        Double temperatureValue,
        String sourceType,
        String qualityFlag
) {
}
