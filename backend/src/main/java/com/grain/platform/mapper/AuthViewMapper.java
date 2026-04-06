package com.grain.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthViewMapper {
    String selectWarehouseNameByUserId(@Param("userId") Long userId);
}
