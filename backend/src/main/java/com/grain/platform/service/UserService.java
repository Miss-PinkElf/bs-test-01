package com.grain.platform.service;

import com.grain.platform.dto.user.RoleOptionResponse;
import com.grain.platform.dto.user.UserListItemResponse;
import com.grain.platform.mapper.RoleMapper;
import com.grain.platform.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public UserService(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    public List<UserListItemResponse> listUsers() {
        return userMapper.selectAllUsers();
    }

    public List<RoleOptionResponse> listRoleOptions() {
        return roleMapper.selectAllRoleOptions();
    }
}
