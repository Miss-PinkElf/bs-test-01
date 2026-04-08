package com.grain.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PredictionResult {
    private Long id;
    private Long taskId;
    private String phaseType;
    private Integer stepIndex;
    private LocalDateTime resultTime;
    private BigDecimal actualValue;
    private BigDecimal predictedValue;
    private BigDecimal errorValue;
    private BigDecimal errorRate;
    private String warningLevel;
    private Boolean warningFlag;
    private String warningMessage;
    private Boolean isCorrected;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PredictionResult() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getPhaseType() { return phaseType; }
    public void setPhaseType(String phaseType) { this.phaseType = phaseType; }
    public Integer getStepIndex() { return stepIndex; }
    public void setStepIndex(Integer stepIndex) { this.stepIndex = stepIndex; }
    public LocalDateTime getResultTime() { return resultTime; }
    public void setResultTime(LocalDateTime resultTime) { this.resultTime = resultTime; }
    public BigDecimal getActualValue() { return actualValue; }
    public void setActualValue(BigDecimal actualValue) { this.actualValue = actualValue; }
    public BigDecimal getPredictedValue() { return predictedValue; }
    public void setPredictedValue(BigDecimal predictedValue) { this.predictedValue = predictedValue; }
    public BigDecimal getErrorValue() { return errorValue; }
    public void setErrorValue(BigDecimal errorValue) { this.errorValue = errorValue; }
    public BigDecimal getErrorRate() { return errorRate; }
    public void setErrorRate(BigDecimal errorRate) { this.errorRate = errorRate; }
    public String getWarningLevel() { return warningLevel; }
    public void setWarningLevel(String warningLevel) { this.warningLevel = warningLevel; }
    public Boolean getWarningFlag() { return warningFlag; }
    public void setWarningFlag(Boolean warningFlag) { this.warningFlag = warningFlag; }
    public String getWarningMessage() { return warningMessage; }
    public void setWarningMessage(String warningMessage) { this.warningMessage = warningMessage; }
    public Boolean getIsCorrected() { return isCorrected; }
    public void setIsCorrected(Boolean corrected) { isCorrected = corrected; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
