package com.grain.platform.service;

import com.grain.platform.common.PageResult;
import com.grain.platform.dto.warehouse.WarehouseDto;
import com.grain.platform.dto.warehouse.WarehouseStatsResponse;
import com.grain.platform.entity.Warehouse;
import com.grain.platform.mapper.WarehouseMapper;
import com.grain.platform.security.CurrentUserContext;
import com.grain.platform.vo.common.IdVO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public List<WarehouseDto> listVisible(CurrentUserContext currentUser) {
        if (currentUser != null && currentUser.warehouseId() != null
                && currentUser.roleCodes() != null
                && currentUser.roleCodes().contains("WAREHOUSE_MANAGER")) {
            Warehouse warehouse = warehouseMapper.selectById(currentUser.warehouseId());
            return warehouse == null ? List.of() : List.of(toDto(warehouse));
        }
        return list();
    }

    public PageResult<WarehouseDto> listPage(String keyword, Integer pageNum, Integer pageSize) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        int finalPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int finalPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = warehouseMapper.countPage(kw);
        int maxPage = total == 0 ? 1 : (int) Math.ceil((double) total / finalPageSize);
        finalPageNum = Math.min(finalPageNum, maxPage);
        int offset = (finalPageNum - 1) * finalPageSize;
        List<WarehouseDto> list = warehouseMapper.selectPage(kw, offset, finalPageSize).stream()
                .map(this::toDto)
                .toList();
        return new PageResult<>(list, finalPageNum, finalPageSize, total);
    }

    public WarehouseStatsResponse stats() {
        // 仓库统计必须走全量聚合，不能拿当前页条数代替顶部卡片口径。
        long totalCount = warehouseMapper.countPage(null);
        long activeCount = warehouseMapper.countActive();
        long nonActiveCount = warehouseMapper.countNonActive();
        return new WarehouseStatsResponse(totalCount, activeCount, nonActiveCount);
    }

    public List<WarehouseDto> options() {
        return warehouseMapper.selectOptions().stream().map(this::toDto).toList();
    }

    public List<WarehouseDto> optionsVisible(CurrentUserContext currentUser) {
        if (currentUser != null && currentUser.warehouseId() != null
                && currentUser.roleCodes() != null
                && currentUser.roleCodes().contains("WAREHOUSE_MANAGER")) {
            Warehouse warehouse = warehouseMapper.selectById(currentUser.warehouseId());
            return warehouse == null ? List.of() : List.of(toDto(warehouse));
        }
        return options();
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
            // 删除失败直接保留“被引用”语义，方便前端区分校验问题和系统异常。
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
