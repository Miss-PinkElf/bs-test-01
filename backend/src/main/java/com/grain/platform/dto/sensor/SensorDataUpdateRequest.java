package com.grain.platform.dto.sensor;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SensorDataUpdateRequest(
        @NotNull(message = "仓库不能为空")
        Long warehouseId,
        @NotBlank(message = "指标编码不能为空")
        String metricCode,
        @NotNull(message = "指标值不能为空")
        Double metricValue,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime collectedAt
) {
}
