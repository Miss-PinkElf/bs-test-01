package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.auth.CurrentUserResponse;
import com.grain.platform.dto.auth.LoginRequest;
import com.grain.platform.dto.auth.LoginResponse;
import com.grain.platform.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 用户登录。
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("调用登录接口，username={}", request.username());
        LoginResponse response = authService.login(request);
        log.info("登录成功，userId={}, username={}, roles={}", response.userId(), response.username(), response.roleCodes());
        return ApiResponse.success(response);
    }

    // 获取当前登录用户。
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@RequestHeader(value = "X-Demo-Username", required = false) String username) {
        String currentUsername = (username == null || username.isBlank()) ? "admin" : username;
        log.info("调用当前用户接口，username={}", currentUsername);
        CurrentUserResponse response = authService.getCurrentUser(currentUsername);
        log.info("获取当前用户成功，userId={}, username={}", response.userId(), response.username());
        return ApiResponse.success(response);
    }
}
