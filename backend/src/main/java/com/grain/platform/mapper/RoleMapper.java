package com.grain.platform.mapper;

import com.grain.platform.dto.user.RoleOptionResponse;
import com.grain.platform.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    List<RoleOptionResponse> selectAllRoleOptions();

    List<SysRole> selectRolesByCodes(@Param("roleCodes") List<String> roleCodes);
}
