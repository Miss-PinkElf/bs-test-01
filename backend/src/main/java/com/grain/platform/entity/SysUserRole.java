package com.grain.platform.entity;

import java.time.LocalDateTime;

public record SysUserRole(
        Long id,
        Long userId,
        Long roleId,
        LocalDateTime createdAt
) {
}
