package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
import com.grain.platform.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 查询仪表盘概览。
    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewResponse> overview() {
        log.info("调用仪表盘概览接口");
        DashboardOverviewResponse response = dashboardService.getOverview();
        log.info("获取仪表盘概览成功，warehouseCount={}, alertCount={}", response.warehouseCount(), response.alertCount());
        return ApiResponse.success(response);
    }
}
