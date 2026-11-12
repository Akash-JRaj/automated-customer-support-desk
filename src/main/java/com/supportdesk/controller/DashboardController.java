package com.supportdesk.controller;

import com.supportdesk.dto.DashboardMetricsDto;
import com.supportdesk.dto.TicketResponseDto;
import com.supportdesk.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Analytics & Dashboard", description = "Performance metrics, resolution rates and AI autonomy tracking")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get aggregate metrics including resolution rates and AI vs Human distribution")
    public ResponseEntity<DashboardMetricsDto> getMetrics() {
        return ResponseEntity.ok(dashboardService.getMetrics());
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get list of recent tickets and status changes")
    public ResponseEntity<List<TicketResponseDto>> getRecentActivity() {
        return ResponseEntity.ok(dashboardService.getRecentActivity());
    }
}
