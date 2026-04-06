package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
import com.grain.platform.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewResponse> overview() {
        return ApiResponse.success(dashboardService.getOverview());
    }
}
