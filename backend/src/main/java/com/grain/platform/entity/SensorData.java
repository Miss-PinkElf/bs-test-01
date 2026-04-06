package com.grain.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SensorData {
    private Long id;
    private Long warehouseId;
    private String metricCode;
    private BigDecimal metricValue;
    private LocalDateTime collectedAt;
    private String sourceType;
    private String sourceBatchNo;
    private String qualityFlag;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SensorData() {
    }

    public SensorData(Long id, Long warehouseId, String metricCode, BigDecimal metricValue, LocalDateTime collectedAt,
                      String sourceType, String sourceBatchNo, String qualityFlag, String remark, Long createdBy,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.warehouseId = warehouseId;
        this.metricCode = metricCode;
        this.metricValue = metricValue;
        this.collectedAt = collectedAt;
        this.sourceType = sourceType;
        this.sourceBatchNo = sourceBatchNo;
        this.qualityFlag = qualityFlag;
        this.remark = remark;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public BigDecimal getMetricValue() { return metricValue; }
    public void setMetricValue(BigDecimal metricValue) { this.metricValue = metricValue; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceBatchNo() { return sourceBatchNo; }
    public void setSourceBatchNo(String sourceBatchNo) { this.sourceBatchNo = sourceBatchNo; }
    public String getQualityFlag() { return qualityFlag; }
    public void setQualityFlag(String qualityFlag) { this.qualityFlag = qualityFlag; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
