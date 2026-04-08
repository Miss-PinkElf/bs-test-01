package com.grain.platform.dto.prediction;

import java.util.List;

public record PredictionTaskResponse(
        Long taskId,
        String taskNo,
        Long parentTaskId,
        Integer taskRound,
        Long warehouseId,
        String warehouseName,
        String metricCode,
        String metricName,
        String unit,
        Double maxThreshold,
        String targetType,
        String dataSourceType,
        String algorithmCode,
        String algorithmName,
        String trainStartTime,
        String trainEndTime,
        String forecastStartTime,
        String forecastEndTime,
        String basedOnActualEndTime,
        Integer forecastDays,
        String triggerType,
        String adjustStatus,
        String riskLevel,
        String requestedAt,
        String completedAt,
        String summary,
        List<PredictionResultItemDto> resultList
) {
}
