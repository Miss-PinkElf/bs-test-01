package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.warehouse.WarehouseDto;
import com.grain.platform.service.WarehouseService;
import com.grain.platform.vo.common.IdVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public ApiResponse<List<WarehouseDto>> list() {
        return ApiResponse.success(warehouseService.list());
    }

    @GetMapping("/options")
    public ApiResponse<List<WarehouseDto>> options() {
        return ApiResponse.success(warehouseService.options());
    }

    @PostMapping
    public ApiResponse<IdVO> create(@Valid @RequestBody WarehouseDto request) {
        return ApiResponse.success(warehouseService.create(request));
    }
}
