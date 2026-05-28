package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.DashboardDTO;
import com.ticketsystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Project-specific dashboard.
     * GET /api/dashboard?projectId=1  → dashboard for project 1
     * GET /api/dashboard              → aggregate across all projects (admin view)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard(
            @RequestParam(required = false) Long projectId) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard loaded",
                dashboardService.getDashboard(projectId)));
    }
}
