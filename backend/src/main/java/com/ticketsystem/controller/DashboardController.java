package com.ticketsystem.controller;

import com.ticketsystem.dto.response.ApiResponse;
import com.ticketsystem.dto.response.DashboardResponse;
import com.ticketsystem.service.impl.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardServiceImpl dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT_ENGINEER')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard()));
    }
}
