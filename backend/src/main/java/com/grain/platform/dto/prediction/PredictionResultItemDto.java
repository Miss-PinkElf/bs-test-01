package com.grain.platform.dto.prediction;

public record PredictionResultItemDto(
        Long id,
        String phaseType,
        Integer stepIndex,
        String resultTime,
        Double actualValue,
        Double predictedValue,
        Double errorValue,
        Double errorRate,
        String warningLevel,
        Boolean warningFlag,
        String warningMessage,
        Boolean isCorrected
) {
}
