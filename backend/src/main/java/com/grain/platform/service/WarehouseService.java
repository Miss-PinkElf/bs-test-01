package com.grain.platform.service;

import com.grain.platform.dto.warehouse.WarehouseDto;
import com.grain.platform.entity.Warehouse;
import com.grain.platform.mapper.WarehouseMapper;
import com.grain.platform.vo.common.IdVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseMapper warehouseMapper;

    public WarehouseService(WarehouseMapper warehouseMapper) {
        this.warehouseMapper = warehouseMapper;
    }

    public List<WarehouseDto> list() {
        return warehouseMapper.selectAll().stream().map(this::toDto).toList();
    }

    public List<WarehouseDto> options() {
        return warehouseMapper.selectOptions().stream().map(this::toDto).toList();
    }

    public IdVO create(WarehouseDto request) {
        Warehouse warehouse = new Warehouse(
                null,
                request.warehouseCode(),
                request.warehouseName(),
                request.location(),
                request.capacityTon() == null ? null : java.math.BigDecimal.valueOf(request.capacityTon()),
                request.managerName(),
                null,
                request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status(),
                null,
                null,
                null,
                null
        );
        warehouseMapper.insert(warehouse);
        return new IdVO(warehouse.getId());
    }

    private WarehouseDto toDto(Warehouse warehouse) {
        return new WarehouseDto(
                warehouse.getId(),
                warehouse.getWarehouseCode(),
                warehouse.getWarehouseName(),
                warehouse.getLocation(),
                warehouse.getCapacityTon() == null ? null : warehouse.getCapacityTon().doubleValue(),
                warehouse.getManagerName(),
                warehouse.getStatus()
        );
    }
}
