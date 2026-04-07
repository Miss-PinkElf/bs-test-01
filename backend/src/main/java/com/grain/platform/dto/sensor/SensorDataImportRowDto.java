package com.grain.platform.dto.sensor;

import java.time.LocalDateTime;

public record SensorDataImportRowDto(
        Long warehouseId,
        String metricCode,
        Double metricValue,
        LocalDateTime collectedAt,
        String remark
) {
}
