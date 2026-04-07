package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.sensor.MetricOptionResponse;
import com.grain.platform.service.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private static final Logger log = LoggerFactory.getLogger(MetricController.class);

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    // 查询启用中的指标选项。
    @GetMapping("/options")
    public ApiResponse<List<MetricOptionResponse>> listOptions() {
        log.info("调用指标选项接口");
        List<MetricOptionResponse> response = metricService.listActiveOptions();
        log.info("获取指标选项成功，count={}", response.size());
        return ApiResponse.success(response);
    }
}
