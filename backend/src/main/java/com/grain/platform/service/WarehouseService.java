package com.grain.platform.service;

import com.grain.platform.dto.warehouse.WarehouseDto;
import com.grain.platform.entity.Warehouse;
import com.grain.platform.mapper.WarehouseMapper;
import com.grain.platform.vo.common.IdVO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        Warehouse warehouse = buildWarehouse(null, request);
        warehouseMapper.insert(warehouse);
        return new IdVO(warehouse.getId());
    }

    public IdVO update(Long id, WarehouseDto request) {
        Warehouse existing = requireWarehouse(id);
        Warehouse warehouse = buildWarehouse(existing.getId(), request);
        warehouseMapper.update(warehouse);
        return new IdVO(id);
    }

    public void delete(Long id) {
        requireWarehouse(id);
        try {
            warehouseMapper.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("仓库已被用户、环境数据或粮温数据引用，暂不能删除");
        }
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

    private Warehouse buildWarehouse(Long id, WarehouseDto request) {
        return new Warehouse(
                id,
                request.warehouseCode(),
                request.warehouseName(),
                request.location(),
                request.capacityTon() == null ? null : BigDecimal.valueOf(request.capacityTon()),
                request.managerName(),
                null,
                request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status(),
                null,
                null,
                null,
                null
        );
    }

    private Warehouse requireWarehouse(Long id) {
        Warehouse warehouse = warehouseMapper.selectById(id);
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库不存在：" + id);
        }
        return warehouse;
    }
}
