package com.grain.platform.mapper;

import com.grain.platform.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WarehouseMapper {
    List<Warehouse> selectAll();

    List<Warehouse> selectOptions();

    void insert(Warehouse warehouse);
}
