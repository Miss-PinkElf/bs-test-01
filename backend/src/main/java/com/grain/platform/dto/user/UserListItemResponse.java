package com.grain.platform.dto.user;

public record UserListItemResponse(
        Long id,
        String username,
        String displayName,
        String roleCodes,
        String roleNames,
        String warehouseName,
        String status,
        String lastLoginAt
) {
}
