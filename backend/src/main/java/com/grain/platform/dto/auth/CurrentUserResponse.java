package com.grain.platform.dto.auth;

import java.util.List;

public record CurrentUserResponse(
        Long userId,
        String username,
        String displayName,
        List<String> roleCodes,
        Long warehouseId,
        String warehouseName
) {
}
