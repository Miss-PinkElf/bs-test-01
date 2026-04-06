package com.grain.platform.entity;

import java.time.LocalDateTime;

public class SysRole {
    private Long id;
    private String roleCode;
    private String roleName;
    private String roleDesc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SysRole() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleDesc() { return roleDesc; }
    public void setRoleDesc(String roleDesc) { this.roleDesc = roleDesc; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
