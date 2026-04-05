package com.finance.dashboard.controller;

import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.DashboardSummary;
import com.finance.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/summary
     * Roles: VIEWER, ANALYST, ADMIN
     * Returns: total income, expenses, net balance, category breakdowns,
     *          recent activity (last 10), and 6-month trend data.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }
}
