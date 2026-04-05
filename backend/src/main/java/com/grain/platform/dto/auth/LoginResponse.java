package com.grain.platform.dto.auth;

public record LoginResponse(
        Long userId,
        String username,
        String displayName,
        String role,
        String token
) {
}
