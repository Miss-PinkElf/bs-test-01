package com.grain.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GrainTempSummary {
    private Long id;
    private Long warehouseId;
    private LocalDateTime collectedAt;
    private BigDecimal avgTemp;
    private BigDecimal maxTemp;
    private BigDecimal minTemp;
    private BigDecimal layer1Avg;
    private BigDecimal layer2Avg;
    private BigDecimal layer3Avg;
    private BigDecimal layer4Avg;
    private String warningLevel;
    private Boolean warningFlag;
    private String warningMessage;
    private String analysisResult;
    private String analysisRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    public BigDecimal getAvgTemp() { return avgTemp; }
    public void setAvgTemp(BigDecimal avgTemp) { this.avgTemp = avgTemp; }
    public BigDecimal getMaxTemp() { return maxTemp; }
    public void setMaxTemp(BigDecimal maxTemp) { this.maxTemp = maxTemp; }
    public BigDecimal getMinTemp() { return minTemp; }
    public void setMinTemp(BigDecimal minTemp) { this.minTemp = minTemp; }
    public BigDecimal getLayer1Avg() { return layer1Avg; }
    public void setLayer1Avg(BigDecimal layer1Avg) { this.layer1Avg = layer1Avg; }
    public BigDecimal getLayer2Avg() { return layer2Avg; }
    public void setLayer2Avg(BigDecimal layer2Avg) { this.layer2Avg = layer2Avg; }
    public BigDecimal getLayer3Avg() { return layer3Avg; }
    public void setLayer3Avg(BigDecimal layer3Avg) { this.layer3Avg = layer3Avg; }
    public BigDecimal getLayer4Avg() { return layer4Avg; }
    public void setLayer4Avg(BigDecimal layer4Avg) { this.layer4Avg = layer4Avg; }
    public String getWarningLevel() { return warningLevel; }
    public void setWarningLevel(String warningLevel) { this.warningLevel = warningLevel; }
    public Boolean getWarningFlag() { return warningFlag; }
    public void setWarningFlag(Boolean warningFlag) { this.warningFlag = warningFlag; }
    public String getWarningMessage() { return warningMessage; }
    public void setWarningMessage(String warningMessage) { this.warningMessage = warningMessage; }
    public String getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(String analysisResult) { this.analysisResult = analysisResult; }
    public String getAnalysisRemark() { return analysisRemark; }
    public void setAnalysisRemark(String analysisRemark) { this.analysisRemark = analysisRemark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
