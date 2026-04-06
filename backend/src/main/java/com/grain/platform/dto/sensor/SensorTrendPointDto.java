package com.grain.platform.dto.sensor;

import java.time.LocalDateTime;

public record SensorTrendPointDto(
        LocalDateTime time,
        Double value
) {
}
