package com.grain.platform.controller;

import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
import com.grain.platform.service.DemoDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DemoDataService demoDataService;

    public DashboardController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @GetMapping("/overview")
    public DashboardOverviewResponse overview() {
        return demoDataService.getOverview();
    }
}
