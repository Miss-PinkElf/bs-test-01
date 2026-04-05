package com.grain.platform.dto.sensor;

import java.time.LocalDateTime;

public record SensorDataPointDto(
        Long id,
        Long warehouseId,
        String warehouseName,
        String metricType,
        Double metricValue,
        LocalDateTime collectedAt
) {
}
