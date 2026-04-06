package com.grain.platform.dto.prediction;

public record PredictionResultItemDto(
        Integer stepIndex,
        String predictedTime,
        Double actualValue,
        Double predictedValue
) {
}
