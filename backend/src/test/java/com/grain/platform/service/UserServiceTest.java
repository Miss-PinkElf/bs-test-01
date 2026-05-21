package com.grain.platform.service;

import com.grain.platform.dto.user.UserCreateRequest;
import com.grain.platform.dto.user.UserUpdateRequest;
import com.grain.platform.entity.SysRole;
import com.grain.platform.entity.SysUser;
import com.grain.platform.mapper.RoleMapper;
import com.grain.platform.mapper.UserMapper;
import com.grain.platform.mapper.WarehouseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserRejectsAdminRoleAssignment() {
        when(roleMapper.selectRolesByCodes(List.of("ADMIN"))).thenReturn(List.of(role(1L, "ADMIN")));

        UserCreateRequest request = new UserCreateRequest(
                "admin_b",
                "123456",
                "第二管理员",
                "",
                null,
                "ACTIVE",
                List.of("ADMIN")
        );

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("系统管理员角色不支持新增分配");

        verify(userMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void updateUserRejectsAdminPromotion() {
        SysUser user = user(2L, "viewer_a");
        when(userMapper.selectById(2L)).thenReturn(user);
        when(roleMapper.selectRoleCodesByUserId(2L)).thenReturn(List.of("VIEWER"));
        when(roleMapper.selectRolesByCodes(List.of("ADMIN"))).thenReturn(List.of(role(1L, "ADMIN")));

        UserUpdateRequest request = new UserUpdateRequest(
                "临时用户",
                "",
                null,
                "ACTIVE",
                List.of("ADMIN")
        );

        assertThatThrownBy(() -> userService.updateUser(2L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("系统管理员角色不支持新增分配");

        verify(userMapper, never()).update(any(SysUser.class));
    }

    @Test
    void updateUserRejectsRemovingAdminRole() {
        SysUser user = user(1L, "admin");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("ADMIN"));
        when(roleMapper.selectRolesByCodes(List.of("VIEWER"))).thenReturn(List.of(role(3L, "VIEWER")));

        UserUpdateRequest request = new UserUpdateRequest(
                "系统管理员",
                "",
                null,
                "ACTIVE",
                List.of("VIEWER")
        );

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("系统管理员角色不能在用户管理中移除");

        verify(userMapper, never()).update(any(SysUser.class));
    }

    @Test
    void deleteUserRejectsAdminUser() {
        SysUser user = user(1L, "admin");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("ADMIN"));

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("系统管理员账号不能删除");

        verify(userMapper, never()).deleteById(1L);
    }

    private SysRole role(Long id, String roleCode) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(roleCode);
        return role;
    }

    private SysUser user(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setDisplayName(username);
        user.setStatus("ACTIVE");
        return user;
    }
}
