package com.grain.platform.dto.sensor;

import java.util.List;

public record SensorTrendResponse(
        List<SensorTrendPointDto> points
) {
}
