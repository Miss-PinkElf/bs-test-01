package com.grain.platform.mapper;

import com.grain.platform.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WarehouseMapper {
    List<Warehouse> selectAll();

    List<Warehouse> selectOptions();

    Warehouse selectById(Long id);

    void insert(Warehouse warehouse);
}
