package com.grain.platform.dto.prediction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PredictionRequest(
        @NotNull(message = "仓库不能为空")
        Long warehouseId,
        @NotBlank(message = "指标不能为空")
        String metricCode,
        String targetType,
        LocalDateTime trainStartTime,
        LocalDateTime trainEndTime,
        @Min(value = 1, message = "预测天数至少为 1")
        int forecastDays
) {
}
