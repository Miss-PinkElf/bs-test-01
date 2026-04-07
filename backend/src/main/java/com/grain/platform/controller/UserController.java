package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.user.RoleOptionResponse;
import com.grain.platform.dto.user.UserListItemResponse;
import com.grain.platform.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 查询用户列表。
    @GetMapping("/users")
    public ApiResponse<List<UserListItemResponse>> listUsers() {
        log.info("调用用户列表接口");
        List<UserListItemResponse> response = userService.listUsers();
        log.info("获取用户列表成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    // 查询角色选项。
    @GetMapping("/roles/options")
    public ApiResponse<List<RoleOptionResponse>> listRoleOptions() {
        log.info("调用角色选项接口");
        List<RoleOptionResponse> response = userService.listRoleOptions();
        log.info("获取角色选项成功，count={}", response.size());
        return ApiResponse.success(response);
    }
}
