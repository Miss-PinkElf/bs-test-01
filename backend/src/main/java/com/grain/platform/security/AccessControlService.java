package com.grain.platform.security;

import com.grain.platform.common.ForbiddenException;
import com.grain.platform.entity.SysUser;
import com.grain.platform.mapper.AuthViewMapper;
import com.grain.platform.mapper.RoleMapper;
import com.grain.platform.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AccessControlService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_WAREHOUSE_MANAGER = "WAREHOUSE_MANAGER";
    private static final String ROLE_VIEWER = "VIEWER";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final AuthViewMapper authViewMapper;

    public AccessControlService(UserMapper userMapper, RoleMapper roleMapper, AuthViewMapper authViewMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.authViewMapper = authViewMapper;
    }

    public CurrentUserContext requireCurrentUser(String usernameHeader) {
        if (!StringUtils.hasText(usernameHeader)) {
            throw new IllegalArgumentException("缺少当前登录用户身份");
        }

        SysUser user = userMapper.selectByUsername(usernameHeader);
        if (user == null) {
            throw new IllegalArgumentException("当前登录用户不存在");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("当前登录用户不可用");
        }

        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(user.getId());
        return new CurrentUserContext(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                roleCodes,
                user.getWarehouseId(),
                authViewMapper.selectWarehouseNameByUserId(user.getId())
        );
    }

    public CurrentUserContext requireAdmin(String usernameHeader) {
        CurrentUserContext currentUser = requireCurrentUser(usernameHeader);
        if (!isAdmin(currentUser)) {
            throw new ForbiddenException("仅管理员可访问该资源");
        }
        return currentUser;
    }

    public boolean isViewer(CurrentUserContext user) {
        return hasRole(user, ROLE_VIEWER);
    }

    public boolean isWarehouseManager(CurrentUserContext user) {
        return hasRole(user, ROLE_WAREHOUSE_MANAGER);
    }

    public boolean isAdmin(CurrentUserContext user) {
        return hasRole(user, ROLE_ADMIN);
    }

    public Long resolveWarehouseScope(CurrentUserContext user, Long requestedWarehouseId) {
        if (isWarehouseManager(user)) {
            if (user.warehouseId() == null) {
                throw new ForbiddenException("当前仓库管理员未绑定仓库");
            }
            return user.warehouseId();
        }

        return requestedWarehouseId;
    }

    public void assertWarehouseWriteAccess(CurrentUserContext user, Long targetWarehouseId) {
        if (isAdmin(user)) {
            return;
        }
        if (isViewer(user)) {
            throw new ForbiddenException("当前角色仅可查看，不能执行写操作");
        }
        if (!isWarehouseManager(user)) {
            throw new ForbiddenException("当前角色无权访问该资源");
        }
        if (user.warehouseId() == null) {
            throw new ForbiddenException("当前仓库管理员未绑定仓库");
        }
        if (targetWarehouseId == null) {
            throw new ForbiddenException("缺少目标仓库，无法校验写权限");
        }
        if (!user.warehouseId().equals(targetWarehouseId)) {
            throw new ForbiddenException("仓库管理员仅可操作所属仓库数据");
        }
    }

    private boolean hasRole(CurrentUserContext user, String roleCode) {
        return user != null && user.roleCodes() != null && user.roleCodes().contains(roleCode);
    }
}
