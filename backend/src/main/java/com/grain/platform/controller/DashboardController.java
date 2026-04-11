package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.common.PageResult;
import com.grain.platform.dto.dashboard.DashboardAlertItemResponse;
import com.grain.platform.dto.dashboard.DashboardLatestSummaryResponse;
import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
import com.grain.platform.dto.dashboard.DashboardWarehouseHealthResponse;
import com.grain.platform.dto.dashboard.ScreenDashboardResponse;
import com.grain.platform.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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
        log.info("获取仪表盘概览成功，warehouseCount={}, realAlertCount={}, predictionAlertCount={}",
                response.warehouseCount(), response.realAlertCount(), response.predictionAlertCount());
        return ApiResponse.success(response);
    }

    @GetMapping("/alerts")
    public ApiResponse<PageResult<DashboardAlertItemResponse>> alerts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        log.info("调用仪表盘预警分页接口，keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        return ApiResponse.success(dashboardService.getAlertPage(keyword, pageNum, pageSize));
    }

    @GetMapping("/warehouse-health")
    public ApiResponse<PageResult<DashboardWarehouseHealthResponse>> warehouseHealth(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        log.info("调用仪表盘仓库健康度分页接口，keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        return ApiResponse.success(dashboardService.getWarehouseHealthPage(keyword, pageNum, pageSize));
    }

    @GetMapping("/grain-summaries")
    public ApiResponse<PageResult<DashboardLatestSummaryResponse>> grainSummaries(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        log.info("调用仪表盘粮温汇总分页接口，keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        return ApiResponse.success(dashboardService.getGrainSummaryPage(keyword, pageNum, pageSize));
    }

    @GetMapping("/screen")
    public ApiResponse<ScreenDashboardResponse> screen(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        log.info("调用展示大屏聚合接口，warehouseId={}, startTime={}, endTime={}", warehouseId, startTime, endTime);
        return ApiResponse.success(dashboardService.getScreenDashboard(warehouseId, startTime, endTime));
    }
}
