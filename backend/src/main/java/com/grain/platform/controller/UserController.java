package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.common.ForbiddenException;
import com.grain.platform.common.PageResult;
import com.grain.platform.dto.user.RoleOptionResponse;
import com.grain.platform.dto.user.UserCreateRequest;
import com.grain.platform.dto.user.UserListItemResponse;
import com.grain.platform.dto.user.UserListStatsResponse;
import com.grain.platform.dto.user.UserPasswordResetRequest;
import com.grain.platform.dto.user.UserUpdateRequest;
import com.grain.platform.security.AccessControlService;
import com.grain.platform.security.CurrentUserContext;
import com.grain.platform.service.UserService;
import com.grain.platform.vo.common.IdVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AccessControlService accessControlService;

    public UserController(UserService userService, AccessControlService accessControlService) {
        this.userService = userService;
        this.accessControlService = accessControlService;
    }

    // 查询用户列表。
    @GetMapping("/users")
    public ApiResponse<List<UserListItemResponse>> listUsers(
            @RequestHeader(value = "X-Demo-Username", required = false) String username
    ) {
        requireUserAdmin(username);
        log.info("调用用户列表接口");
        List<UserListItemResponse> response = userService.listUsers();
        log.info("获取用户列表成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    @GetMapping("/users/page")
    public ApiResponse<PageResult<UserListItemResponse>> listUsersPage(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        requireUserAdmin(username);
        log.info("调用用户分页列表接口，keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        PageResult<UserListItemResponse> response = userService.listUsersPage(keyword, pageNum, pageSize);
        log.info("获取用户分页列表成功，pageNum={}, pageSize={}, total={}",
                response.pageNum(), response.pageSize(), response.total());
        return ApiResponse.success(response);
    }

    @GetMapping("/users/stats")
    public ApiResponse<UserListStatsResponse> userStats(
            @RequestHeader(value = "X-Demo-Username", required = false) String username
    ) {
        requireUserAdmin(username);
        log.info("调用用户统计接口");
        UserListStatsResponse response = userService.listUserStats();
        log.info("获取用户统计成功，totalUsers={}, activeUsers={}", response.totalUsers(), response.activeUsers());
        return ApiResponse.success(response);
    }

    // 新增用户。
    @PostMapping("/users")
    public ApiResponse<IdVO> createUser(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @Valid @RequestBody UserCreateRequest request
    ) {
        requireUserAdmin(username);
        log.info("调用新增用户接口，username={}", request.username());
        IdVO response = userService.createUser(request);
        log.info("新增用户成功，id={}", response.id());
        return ApiResponse.success(response);
    }

    // 编辑用户。
    @PutMapping("/users/{id}")
    public ApiResponse<IdVO> updateUser(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        requireUserAdmin(username);
        log.info("调用编辑用户接口，id={}", id);
        IdVO response = userService.updateUser(id, request);
        log.info("编辑用户成功，id={}", response.id());
        return ApiResponse.success(response);
    }

    // 重置密码。
    @PutMapping("/users/{id}/password")
    public ApiResponse<IdVO> resetPassword(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordResetRequest request
    ) {
        requireUserAdmin(username);
        log.info("调用重置密码接口，id={}", id);
        IdVO response = userService.resetPassword(id, request);
        log.info("重置密码成功，id={}", response.id());
        return ApiResponse.success(response);
    }

    // 删除用户。
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @PathVariable Long id
    ) {
        requireUserAdmin(username);
        log.info("调用删除用户接口，id={}", id);
        userService.deleteUser(id);
        log.info("删除用户成功，id={}", id);
        return ApiResponse.success(null);
    }

    // 查询角色选项。
    @GetMapping("/roles/options")
    public ApiResponse<List<RoleOptionResponse>> listRoleOptions(
            @RequestHeader(value = "X-Demo-Username", required = false) String username
    ) {
        requireUserAdmin(username);
        log.info("调用角色选项接口");
        List<RoleOptionResponse> response = userService.listRoleOptions();
        log.info("获取角色选项成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    private void requireUserAdmin(String username) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        if (!accessControlService.isAdmin(currentUser)) {
            throw new ForbiddenException("仅管理员可管理用户");
        }
    }
}
