package com.grain.platform.dto.sensor;

import java.util.List;

public record SensorDataImportResultDto(
        int successCount,
        int failedCount,
        String batchNo,
        List<String> errorMessages
) {
}
