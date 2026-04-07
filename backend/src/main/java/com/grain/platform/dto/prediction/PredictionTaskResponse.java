package com.grain.platform.dto.prediction;

import java.util.List;

public record PredictionTaskResponse(
        Long taskId,
        String taskNo,
        String metricCode,
        String metricName,
        String unit,
        Double maxThreshold,
        String algorithmName,
        String riskLevel,
        String requestedAt,
        String summary,
        List<PredictionResultItemDto> resultList
) {
}
