package com.grain.platform.dto.user;

public record UserListStatsResponse(
        long totalUsers,
        long activeUsers
) {
}
