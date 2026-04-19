package com.grain.platform.mapper;

import com.grain.platform.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarehouseMapper {
    List<Warehouse> selectAll();

    long countPage(@Param("keyword") String keyword);

    List<Warehouse> selectPage(@Param("keyword") String keyword,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    List<Warehouse> selectOptions();

    Warehouse selectById(Long id);

    Warehouse selectByWarehouseCode(@Param("warehouseCode") String warehouseCode);

    long countActive();

    long countNonActive();

    void insert(Warehouse warehouse);

    void update(Warehouse warehouse);

    void deleteById(Long id);
}
