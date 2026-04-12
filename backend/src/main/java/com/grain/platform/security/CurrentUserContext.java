package com.grain.platform.security;

import java.util.List;

public record CurrentUserContext(
        Long userId,
        String username,
        String displayName,
        List<String> roleCodes,
        Long warehouseId,
        String warehouseName
) {
}
