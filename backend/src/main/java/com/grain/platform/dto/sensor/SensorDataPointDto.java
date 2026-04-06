package com.grain.platform.dto.sensor;

import java.time.LocalDateTime;

public class SensorDataPointDto {
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private String metricCode;
    private String metricName;
    private Double metricValue;
    private String unit;
    private LocalDateTime collectedAt;
    private String sourceType;
    private String qualityFlag;

    public SensorDataPointDto() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public Double getMetricValue() { return metricValue; }
    public void setMetricValue(Double metricValue) { this.metricValue = metricValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getQualityFlag() { return qualityFlag; }
    public void setQualityFlag(String qualityFlag) { this.qualityFlag = qualityFlag; }
}
