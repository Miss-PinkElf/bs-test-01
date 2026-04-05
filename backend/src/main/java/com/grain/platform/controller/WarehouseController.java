package com.grain.platform.controller;

import com.grain.platform.dto.warehouse.WarehouseDto;
import com.grain.platform.service.DemoDataService;
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

    private final DemoDataService demoDataService;

    public WarehouseController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    public List<WarehouseDto> list() {
        return demoDataService.listWarehouses();
    }

    @PostMapping
    public WarehouseDto create(@Valid @RequestBody WarehouseDto request) {
        return demoDataService.createWarehouse(request);
    }
}
