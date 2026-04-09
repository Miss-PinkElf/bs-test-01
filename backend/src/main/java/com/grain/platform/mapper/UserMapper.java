package com.grain.platform.mapper;

import com.grain.platform.dto.user.UserListItemResponse;
import com.grain.platform.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    SysUser selectByUsername(@Param("username") String username);

    SysUser selectById(@Param("id") Long id);

    List<UserListItemResponse> selectAllUsers();

    void insert(SysUser user);

    void update(SysUser user);

    void updatePassword(@Param("id") Long id, @Param("password") String password);

    void deleteById(@Param("id") Long id);

    void deleteUserRolesByUserId(@Param("userId") Long userId);

    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int countUserReferences(@Param("userId") Long userId);

    int countActiveUsersByRoleCode(@Param("roleCode") String roleCode);

    void updateLastLoginAt(@Param("id") Long id);
}
