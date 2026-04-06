package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.sensor.SensorDataCreateRequest;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.dto.sensor.SensorTrendResponse;
import com.grain.platform.service.SensorDataService;
import com.grain.platform.vo.common.IdVO;
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

    private final SensorDataService sensorDataService;

    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    @GetMapping
    public ApiResponse<List<SensorDataPointDto>> list(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) String metricType
    ) {
        String finalMetricCode = (metricCode == null || metricCode.isBlank()) ? metricType : metricCode;
        return ApiResponse.success(sensorDataService.list(warehouseId, finalMetricCode));
    }

    @GetMapping("/trend")
    public ApiResponse<SensorTrendResponse> trend(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) String metricType
    ) {
        String finalMetricCode = (metricCode == null || metricCode.isBlank()) ? metricType : metricCode;
        return ApiResponse.success(sensorDataService.trend(warehouseId, finalMetricCode));
    }

    @PostMapping
    public ApiResponse<IdVO> create(@Valid @RequestBody SensorDataCreateRequest request) {
        return ApiResponse.success(sensorDataService.create(request));
    }
}
