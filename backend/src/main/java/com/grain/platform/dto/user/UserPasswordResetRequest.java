package com.grain.platform.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordResetRequest(
        @NotBlank(message = "新密码不能为空")
        String newPassword
) {
}
