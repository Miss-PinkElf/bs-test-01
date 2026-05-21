package com.grain.platform.dto.prediction;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime trainStartTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime trainEndTime,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime forecastStartTime,
        @Min(value = 1, message = "预测天数至少为 1")
        int forecastDays
) {
}
