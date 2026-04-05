package com.grain.platform.controller;

import com.grain.platform.dto.sensor.SensorDataCreateRequest;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.service.DemoDataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sensor-data")
public class SensorDataController {

    private final DemoDataService demoDataService;

    public SensorDataController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping
    public List<SensorDataPointDto> list(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String metricType
    ) {
        return demoDataService.querySensorData(warehouseId, metricType);
    }

    @PostMapping
    public SensorDataPointDto create(@Valid @RequestBody SensorDataCreateRequest request) {
        return demoDataService.createSensorData(request);
    }
}
