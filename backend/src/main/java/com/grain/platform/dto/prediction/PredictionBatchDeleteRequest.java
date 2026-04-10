package com.grain.platform.dto.prediction;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PredictionBatchDeleteRequest(
        @NotEmpty(message = "请选择要删除的预测任务")
        List<Long> taskIds
) {
}
