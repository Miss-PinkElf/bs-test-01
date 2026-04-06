package com.grain.platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Warehouse {
    private Long id;
    private String warehouseCode;
    private String warehouseName;
    private String location;
    private BigDecimal capacityTon;
    private String managerName;
    private String contactPhone;
    private String status;
    private String grainType;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Warehouse() {
    }

    public Warehouse(Long id, String warehouseCode, String warehouseName, String location, BigDecimal capacityTon,
                     String managerName, String contactPhone, String status, String grainType, String remark,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.warehouseCode = warehouseCode;
        this.warehouseName = warehouseName;
        this.location = location;
        this.capacityTon = capacityTon;
        this.managerName = managerName;
        this.contactPhone = contactPhone;
        this.status = status;
        this.grainType = grainType;
        this.remark = remark;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getCapacityTon() { return capacityTon; }
    public void setCapacityTon(BigDecimal capacityTon) { this.capacityTon = capacityTon; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGrainType() { return grainType; }
    public void setGrainType(String grainType) { this.grainType = grainType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
