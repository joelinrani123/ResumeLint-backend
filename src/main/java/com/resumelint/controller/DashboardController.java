package com.resumelint.controller;

import com.resumelint.dto.DashboardStatsDto;
import com.resumelint.security.CurrentUser;
import com.resumelint.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Equivalent of routes/dashboard.ts. Protected via AuthInterceptor. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    public DashboardController(DashboardService dashboardService, CurrentUser currentUser) {
        this.dashboardService = dashboardService;
        this.currentUser = currentUser;
    }

    @GetMapping("/stats")
    public DashboardStatsDto stats() {
        return dashboardService.getStats(currentUser.get().userId());
    }
}
