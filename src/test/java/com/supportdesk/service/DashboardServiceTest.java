package com.supportdesk.service;

import com.supportdesk.dto.DashboardMetricsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should calculate KPI metrics, resolution rates, and handling distributions")
    void testDashboardMetricsCalculation() {
        DashboardMetricsDto metrics = dashboardService.getMetrics();

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalTickets()).isGreaterThan(0);
        assertThat(metrics.getResolutionRate()).isBetween(0.0, 100.0);
        assertThat(metrics.getAiAutonomyRate()).isBetween(0.0, 100.0);
        assertThat(metrics.getCategoryDistribution()).isNotEmpty();
        assertThat(metrics.getUrgencyDistribution()).isNotEmpty();
    }
}
