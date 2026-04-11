package com.grain.platform.service;

import com.grain.platform.common.PageResult;
import com.grain.platform.dto.user.RoleOptionResponse;
import com.grain.platform.dto.user.UserCreateRequest;
import com.grain.platform.dto.user.UserListItemResponse;
import com.grain.platform.dto.user.UserListStatsResponse;
import com.grain.platform.dto.user.UserPasswordResetRequest;
import com.grain.platform.dto.user.UserUpdateRequest;
import com.grain.platform.entity.SysRole;
import com.grain.platform.entity.SysUser;
import com.grain.platform.mapper.RoleMapper;
import com.grain.platform.mapper.UserMapper;
import com.grain.platform.mapper.WarehouseMapper;
import com.grain.platform.vo.common.IdVO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {

    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DISABLED_STATUS = "DISABLED";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final WarehouseMapper warehouseMapper;

    public UserService(UserMapper userMapper, RoleMapper roleMapper, WarehouseMapper warehouseMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.warehouseMapper = warehouseMapper;
    }

    public List<UserListItemResponse> listUsers() {
        return userMapper.selectAllUsers();
    }

    public PageResult<UserListItemResponse> listUsersPage(String keyword, Integer pageNum, Integer pageSize) {
        // 用户分页统一在 service 层做页码和关键词归一化，控制器只负责转发查询参数。
        String kw = normalizeKeyword(keyword);
        int finalPageNum = normalizePageNum(pageNum);
        int finalPageSize = normalizePageSize(pageSize);
        long total = userMapper.countUserPage(kw);
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        List<UserListItemResponse> list = userMapper.selectUserPage(kw, offset, finalPageSize);
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }

    public UserListStatsResponse listUserStats() {
        return userMapper.selectUserStats();
    }

    public List<RoleOptionResponse> listRoleOptions() {
        return roleMapper.selectAllRoleOptions();
    }

    @Transactional
    public IdVO createUser(UserCreateRequest request) {
        if (userMapper.selectByUsername(request.username()) != null) {
            throw new IllegalArgumentException("用户名已存在：" + request.username());
        }

        Long warehouseId = normalizeWarehouseId(request.warehouseId());
        requireWarehouseIfPresent(warehouseId);
        String status = normalizeStatus(request.status());
        List<SysRole> roles = resolveRoles(request.roleCodes());

        SysUser user = new SysUser();
        user.setUsername(request.username().trim());
        user.setPassword(request.password().trim());
        user.setDisplayName(request.displayName().trim());
        user.setPhone(normalizePhone(request.phone()));
        user.setWarehouseId(warehouseId);
        user.setStatus(status);

        try {
            userMapper.insert(user);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("用户名已存在：" + request.username());
        }
        rebuildUserRoles(user.getId(), roles);
        return new IdVO(user.getId());
    }

    @Transactional
    public IdVO updateUser(Long id, UserUpdateRequest request) {
        SysUser user = requireUser(id);
        List<String> existingRoleCodes = roleMapper.selectRoleCodesByUserId(id);
        String nextStatus = normalizeStatus(request.status());
        List<SysRole> roles = resolveRoles(request.roleCodes());
        ensureNotLastActiveAdmin(existingRoleCodes, user.getStatus(), extractRoleCodes(roles), nextStatus);

        Long warehouseId = normalizeWarehouseId(request.warehouseId());
        requireWarehouseIfPresent(warehouseId);

        user.setDisplayName(request.displayName().trim());
        user.setPhone(normalizePhone(request.phone()));
        user.setWarehouseId(warehouseId);
        user.setStatus(nextStatus);
        userMapper.update(user);
        rebuildUserRoles(id, roles);
        return new IdVO(id);
    }

    @Transactional
    public IdVO resetPassword(Long id, UserPasswordResetRequest request) {
        requireUser(id);
        userMapper.updatePassword(id, request.newPassword().trim());
        return new IdVO(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        SysUser user = requireUser(id);
        List<String> existingRoleCodes = roleMapper.selectRoleCodesByUserId(id);
        ensureNotLastActiveAdmin(existingRoleCodes, user.getStatus(), List.of(), DISABLED_STATUS);

        if (userMapper.countUserReferences(id) > 0) {
            throw new IllegalArgumentException("用户已被数据录入或预测记录引用，暂不能删除");
        }

        userMapper.deleteUserRolesByUserId(id);
        userMapper.deleteById(id);
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在：" + id);
        }
        return user;
    }

    private void requireWarehouseIfPresent(Long warehouseId) {
        if (warehouseId != null && warehouseMapper.selectById(warehouseId) == null) {
            throw new IllegalArgumentException("仓库不存在：" + warehouseId);
        }
    }

    private String normalizePhone(String phone) {
        return StringUtils.hasText(phone) ? phone.trim() : null;
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private Long normalizeWarehouseId(Long warehouseId) {
        return warehouseId == null || warehouseId <= 0 ? null : warehouseId;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private String normalizeStatus(String status) {
        String normalized = StringUtils.hasText(status) ? status.trim().toUpperCase(Locale.ROOT) : ACTIVE_STATUS;
        if (!ACTIVE_STATUS.equals(normalized) && !DISABLED_STATUS.equals(normalized)) {
            throw new IllegalArgumentException("用户状态仅支持 ACTIVE 或 DISABLED");
        }
        return normalized;
    }

    private List<SysRole> resolveRoles(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new IllegalArgumentException("至少选择一个角色");
        }

        // 角色集合先去重再校验，避免前端重复提交时把同一个角色插入多次。
        Set<String> normalizedCodes = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            if (!StringUtils.hasText(roleCode)) {
                continue;
            }
            normalizedCodes.add(roleCode.trim().toUpperCase(Locale.ROOT));
        }

        if (normalizedCodes.isEmpty()) {
            throw new IllegalArgumentException("至少选择一个角色");
        }

        List<SysRole> roles = roleMapper.selectRolesByCodes(List.copyOf(normalizedCodes));
        if (roles.size() != normalizedCodes.size()) {
            throw new IllegalArgumentException("存在无效角色编码，请刷新后重试");
        }
        return roles;
    }

    private List<String> extractRoleCodes(List<SysRole> roles) {
        return roles.stream().map(SysRole::getRoleCode).toList();
    }

    private void rebuildUserRoles(Long userId, List<SysRole> roles) {
        userMapper.deleteUserRolesByUserId(userId);
        for (SysRole role : roles) {
            userMapper.insertUserRole(userId, role.getId());
        }
    }

    private void ensureNotLastActiveAdmin(List<String> existingRoleCodes, String existingStatus, List<String> nextRoleCodes, String nextStatus) {
        boolean existingActiveAdmin = isActiveAdmin(existingRoleCodes, existingStatus);
        boolean nextActiveAdmin = isActiveAdmin(nextRoleCodes, nextStatus);

        // 至少保留一个启用中的管理员账号，删除或降权都要先过这道守卫。
        if (existingActiveAdmin && !nextActiveAdmin && userMapper.countActiveUsersByRoleCode(ADMIN_ROLE_CODE) <= 1) {
            throw new IllegalArgumentException("系统至少需要保留一个启用中的管理员账号");
        }
    }

    private boolean isActiveAdmin(List<String> roleCodes, String status) {
        return ACTIVE_STATUS.equalsIgnoreCase(status) && roleCodes.stream().anyMatch(ADMIN_ROLE_CODE::equalsIgnoreCase);
    }
}
