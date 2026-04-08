package com.grain.platform.entity;

import java.time.LocalDateTime;

public class GrainTempPoint {
    private Long id;
    private Long warehouseId;
    private String probeCode;
    private String zoneCode;
    private Integer layerNo;
    private Integer pointNo;
    private String pointName;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getProbeCode() { return probeCode; }
    public void setProbeCode(String probeCode) { this.probeCode = probeCode; }
    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }
    public Integer getLayerNo() { return layerNo; }
    public void setLayerNo(Integer layerNo) { this.layerNo = layerNo; }
    public Integer getPointNo() { return pointNo; }
    public void setPointNo(Integer pointNo) { this.pointNo = pointNo; }
    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
