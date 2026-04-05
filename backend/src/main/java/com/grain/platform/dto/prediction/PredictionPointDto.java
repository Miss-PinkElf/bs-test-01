package com.grain.platform.dto.prediction;

import java.time.LocalDateTime;

public record PredictionPointDto(
        LocalDateTime time,
        Double actualValue,
        Double predictedValue
) {
}
