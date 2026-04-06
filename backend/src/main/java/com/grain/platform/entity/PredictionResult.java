package com.grain.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PredictionResult {
    private Long id;
    private Long taskId;
    private Integer stepIndex;
    private LocalDateTime predictedTime;
    private BigDecimal actualValue;
    private BigDecimal predictedValue;
    private LocalDateTime createdAt;

    public PredictionResult() {
    }

    public PredictionResult(Long id, Long taskId, Integer stepIndex, LocalDateTime predictedTime,
                            BigDecimal actualValue, BigDecimal predictedValue, LocalDateTime createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.stepIndex = stepIndex;
        this.predictedTime = predictedTime;
        this.actualValue = actualValue;
        this.predictedValue = predictedValue;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Integer getStepIndex() { return stepIndex; }
    public void setStepIndex(Integer stepIndex) { this.stepIndex = stepIndex; }
    public LocalDateTime getPredictedTime() { return predictedTime; }
    public void setPredictedTime(LocalDateTime predictedTime) { this.predictedTime = predictedTime; }
    public BigDecimal getActualValue() { return actualValue; }
    public void setActualValue(BigDecimal actualValue) { this.actualValue = actualValue; }
    public BigDecimal getPredictedValue() { return predictedValue; }
    public void setPredictedValue(BigDecimal predictedValue) { this.predictedValue = predictedValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
