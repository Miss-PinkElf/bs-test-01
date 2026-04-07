package com.grain.platform.mapper;

import com.grain.platform.dto.user.UserListItemResponse;
import com.grain.platform.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    SysUser selectByUsername(@Param("username") String username);

    List<UserListItemResponse> selectAllUsers();

    void updateLastLoginAt(@Param("id") Long id);
}
