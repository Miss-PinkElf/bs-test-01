package com.grain.platform.entity;

import java.time.LocalDateTime;

public class PredictionTask {
    private Long id;
    private String taskNo;
    private Long warehouseId;
    private String metricCode;
    private String algorithmCode;
    private String algorithmName;
    private Integer futureSteps;
    private Integer sampleSize;
    private String status;
    private String riskLevel;
    private Long requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PredictionTask() {
    }

    public PredictionTask(Long id, String taskNo, Long warehouseId, String metricCode, String algorithmCode,
                          String algorithmName, Integer futureSteps, Integer sampleSize, String status,
                          String riskLevel, Long requestedBy, LocalDateTime requestedAt, LocalDateTime completedAt,
                          String summary, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.taskNo = taskNo;
        this.warehouseId = warehouseId;
        this.metricCode = metricCode;
        this.algorithmCode = algorithmCode;
        this.algorithmName = algorithmName;
        this.futureSteps = futureSteps;
        this.sampleSize = sampleSize;
        this.status = status;
        this.riskLevel = riskLevel;
        this.requestedBy = requestedBy;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.summary = summary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getAlgorithmCode() { return algorithmCode; }
    public void setAlgorithmCode(String algorithmCode) { this.algorithmCode = algorithmCode; }
    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }
    public Integer getFutureSteps() { return futureSteps; }
    public void setFutureSteps(Integer futureSteps) { this.futureSteps = futureSteps; }
    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Long getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Long requestedBy) { this.requestedBy = requestedBy; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
