package com.grain.platform.dto.dashboard;

public record ScreenTrendPointResponse(
        String timeLabel,
        Double primaryValue,
        Double secondaryValue,
        Integer realAlertCount,
        Integer predictionAlertCount
) {
}
