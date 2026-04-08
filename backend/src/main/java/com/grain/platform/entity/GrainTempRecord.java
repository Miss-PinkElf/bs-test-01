package com.grain.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GrainTempRecord {
    private Long id;
    private Long warehouseId;
    private Long pointId;
    private LocalDateTime collectedAt;
    private BigDecimal temperatureValue;
    private String sourceType;
    private String batchNo;
    private String qualityFlag;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getPointId() { return pointId; }
    public void setPointId(Long pointId) { this.pointId = pointId; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    public BigDecimal getTemperatureValue() { return temperatureValue; }
    public void setTemperatureValue(BigDecimal temperatureValue) { this.temperatureValue = temperatureValue; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
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
