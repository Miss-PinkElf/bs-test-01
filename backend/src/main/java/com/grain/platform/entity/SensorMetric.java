package com.grain.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SensorMetric {
    private Long id;
    private String metricCode;
    private String metricName;
    private String unit;
    private BigDecimal minThreshold;
    private BigDecimal maxThreshold;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SensorMetric() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getMinThreshold() { return minThreshold; }
    public void setMinThreshold(BigDecimal minThreshold) { this.minThreshold = minThreshold; }
    public BigDecimal getMaxThreshold() { return maxThreshold; }
    public void setMaxThreshold(BigDecimal maxThreshold) { this.maxThreshold = maxThreshold; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
