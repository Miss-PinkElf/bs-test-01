package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.sensor.SensorDataCreateRequest;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.dto.sensor.SensorTrendResponse;
import com.grain.platform.service.SensorDataService;
import com.grain.platform.vo.common.IdVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(SensorDataController.class);

    private final SensorDataService sensorDataService;

    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    // 查询环境数据列表。
    @GetMapping
    public ApiResponse<List<SensorDataPointDto>> list(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) String metricType
    ) {
        String finalMetricCode = (metricCode == null || metricCode.isBlank()) ? metricType : metricCode;
        log.info("调用环境数据列表接口，warehouseId={}, metricCode={}", warehouseId, finalMetricCode);
        List<SensorDataPointDto> response = sensorDataService.list(warehouseId, finalMetricCode);
        log.info("获取环境数据列表成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    // 查询环境趋势数据。
    @GetMapping("/trend")
    public ApiResponse<SensorTrendResponse> trend(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) String metricType
    ) {
        String finalMetricCode = (metricCode == null || metricCode.isBlank()) ? metricType : metricCode;
        log.info("调用环境趋势接口，warehouseId={}, metricCode={}", warehouseId, finalMetricCode);
        SensorTrendResponse response = sensorDataService.trend(warehouseId, finalMetricCode);
        int pointCount = response.points() == null ? 0 : response.points().size();
        log.info("获取环境趋势成功，pointCount={}", pointCount);
        return ApiResponse.success(response);
    }

    // 新增环境数据。
    @PostMapping
    public ApiResponse<IdVO> create(@Valid @RequestBody SensorDataCreateRequest request) {
        log.info("调用新增环境数据接口，warehouseId={}, metricCode={}, metricValue={}", request.warehouseId(), request.metricCode(), request.metricValue());
        IdVO response = sensorDataService.create(request);
        log.info("新增环境数据成功，id={}", response.id());
        return ApiResponse.success(response);
    }
}
