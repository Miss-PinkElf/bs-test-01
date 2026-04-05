package com.grain.platform.controller;

import com.grain.platform.dto.auth.LoginRequest;
import com.grain.platform.dto.auth.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String role = "admin".equalsIgnoreCase(request.username()) ? "ADMIN" : "WAREHOUSE_MANAGER";
        return new LoginResponse(1L, request.username(), "演示用户", role, "demo-token-" + request.username());
    }
}
