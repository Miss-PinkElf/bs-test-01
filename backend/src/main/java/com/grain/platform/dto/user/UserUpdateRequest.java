package com.grain.platform.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UserUpdateRequest(
        @NotBlank(message = "姓名不能为空")
        String displayName,
        String phone,
        Long warehouseId,
        String status,
        @NotEmpty(message = "至少选择一个角色")
        List<@NotBlank(message = "角色编码不能为空") String> roleCodes
) {
}
