package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.auth.CurrentUserResponse;
import com.grain.platform.dto.auth.LoginRequest;
import com.grain.platform.dto.auth.LoginResponse;
import com.grain.platform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@RequestHeader(value = "X-Demo-Username", required = false) String username) {
        String currentUsername = (username == null || username.isBlank()) ? "admin" : username;
        return ApiResponse.success(authService.getCurrentUser(currentUsername));
    }
}
