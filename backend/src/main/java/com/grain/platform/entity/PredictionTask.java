package com.grain.platform.entity;

import java.time.LocalDateTime;

public class PredictionTask {
    private Long id;
    private String taskNo;
    private Long parentTaskId;
    private Integer taskRound;
    private Long warehouseId;
    private String metricCode;
    private String dataSourceType;
    private String targetType;
    private String algorithmCode;
    private String algorithmName;
    private LocalDateTime trainStartTime;
    private LocalDateTime trainEndTime;
    private LocalDateTime forecastStartTime;
    private LocalDateTime forecastEndTime;
    private LocalDateTime basedOnActualEndTime;
    private Integer forecastDays;
    private Integer sampleSize;
    private String triggerType;
    private String adjustStatus;
    private String status;
    private String riskLevel;
    private Long requestedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String summary;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PredictionTask() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public Long getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(Long parentTaskId) { this.parentTaskId = parentTaskId; }
    public Integer getTaskRound() { return taskRound; }
    public void setTaskRound(Integer taskRound) { this.taskRound = taskRound; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getDataSourceType() { return dataSourceType; }
    public void setDataSourceType(String dataSourceType) { this.dataSourceType = dataSourceType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getAlgorithmCode() { return algorithmCode; }
    public void setAlgorithmCode(String algorithmCode) { this.algorithmCode = algorithmCode; }
    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }
    public LocalDateTime getTrainStartTime() { return trainStartTime; }
    public void setTrainStartTime(LocalDateTime trainStartTime) { this.trainStartTime = trainStartTime; }
    public LocalDateTime getTrainEndTime() { return trainEndTime; }
    public void setTrainEndTime(LocalDateTime trainEndTime) { this.trainEndTime = trainEndTime; }
    public LocalDateTime getForecastStartTime() { return forecastStartTime; }
    public void setForecastStartTime(LocalDateTime forecastStartTime) { this.forecastStartTime = forecastStartTime; }
    public LocalDateTime getForecastEndTime() { return forecastEndTime; }
    public void setForecastEndTime(LocalDateTime forecastEndTime) { this.forecastEndTime = forecastEndTime; }
    public LocalDateTime getBasedOnActualEndTime() { return basedOnActualEndTime; }
    public void setBasedOnActualEndTime(LocalDateTime basedOnActualEndTime) { this.basedOnActualEndTime = basedOnActualEndTime; }
    public Integer getForecastDays() { return forecastDays; }
    public void setForecastDays(Integer forecastDays) { this.forecastDays = forecastDays; }
    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public String getAdjustStatus() { return adjustStatus; }
    public void setAdjustStatus(String adjustStatus) { this.adjustStatus = adjustStatus; }
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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
