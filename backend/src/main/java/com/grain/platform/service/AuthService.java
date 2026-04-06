package com.grain.platform.service;

import com.grain.platform.dto.auth.CurrentUserResponse;
import com.grain.platform.dto.auth.LoginRequest;
import com.grain.platform.dto.auth.LoginResponse;
import com.grain.platform.entity.SysUser;
import com.grain.platform.mapper.AuthViewMapper;
import com.grain.platform.mapper.RoleMapper;
import com.grain.platform.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final AuthViewMapper authViewMapper;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper, AuthViewMapper authViewMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.authViewMapper = authViewMapper;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectByUsername(request.username());
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!StringUtils.hasText(request.password()) || !request.password().equals(user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(user.getId());
        String warehouseName = authViewMapper.selectWarehouseNameByUserId(user.getId());
        userMapper.updateLastLoginAt(user.getId());

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                roleCodes,
                user.getWarehouseId(),
                warehouseName,
                "demo-token-" + user.getUsername()
        );
    }

    public CurrentUserResponse getCurrentUser(String username) {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                roleMapper.selectRoleCodesByUserId(user.getId()),
                user.getWarehouseId(),
                authViewMapper.selectWarehouseNameByUserId(user.getId())
        );
    }
}
