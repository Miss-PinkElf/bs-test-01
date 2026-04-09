package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.warehouse.WarehouseDto;
import com.grain.platform.service.WarehouseService;
import com.grain.platform.vo.common.IdVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private static final Logger log = LoggerFactory.getLogger(WarehouseController.class);

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    // 查询仓库列表。
    @GetMapping
    public ApiResponse<List<WarehouseDto>> list() {
        log.info("调用仓库列表接口");
        List<WarehouseDto> response = warehouseService.list();
        log.info("获取仓库列表成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    // 查询仓库下拉选项。
    @GetMapping("/options")
    public ApiResponse<List<WarehouseDto>> options() {
        log.info("调用仓库选项接口");
        List<WarehouseDto> response = warehouseService.options();
        log.info("获取仓库选项成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    // 新增仓库。
    @PostMapping
    public ApiResponse<IdVO> create(@Valid @RequestBody WarehouseDto request) {
        log.info("调用新增仓库接口，warehouseCode={}, warehouseName={}", request.warehouseCode(), request.warehouseName());
        IdVO response = warehouseService.create(request);
        log.info("新增仓库成功，id={}", response.id());
        return ApiResponse.success(response);
    }

    // 编辑仓库。
    @PutMapping("/{id}")
    public ApiResponse<IdVO> update(@PathVariable Long id, @Valid @RequestBody WarehouseDto request) {
        log.info("调用编辑仓库接口，id={}, warehouseCode={}, warehouseName={}", id, request.warehouseCode(), request.warehouseName());
        IdVO response = warehouseService.update(id, request);
        log.info("编辑仓库成功，id={}", response.id());
        return ApiResponse.success(response);
    }

    // 删除仓库。
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("调用删除仓库接口，id={}", id);
        warehouseService.delete(id);
        log.info("删除仓库成功，id={}", id);
        return ApiResponse.success(null);
    }
}
