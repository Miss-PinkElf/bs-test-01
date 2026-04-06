package com.grain.platform.mapper;

import com.grain.platform.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    SysUser selectByUsername(@Param("username") String username);

    void updateLastLoginAt(@Param("id") Long id);
}
