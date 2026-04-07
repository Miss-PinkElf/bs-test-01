package com.grain.platform.dto.sensor;

import java.math.BigDecimal;

public record MetricOptionResponse(
        String metricCode,
        String metricName,
        String unit,
        BigDecimal minThreshold,
        BigDecimal maxThreshold
) {
}
