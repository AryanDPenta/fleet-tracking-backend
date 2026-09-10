package com.fleet.tracking.controller;

import com.fleet.tracking.dto.DriverDaySummaryDTO;
import com.fleet.tracking.security.AuthenticatedUser;
import com.fleet.tracking.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverDashboardController {

    private final AnalyticsService analyticsService;

    // Driver's own day-by-day drive time vs threshold history, including
    // any carried-over shortfall from previous days.
    @GetMapping("/summary")
    public List<DriverDaySummaryDTO> getMySummary(@AuthenticationPrincipal AuthenticatedUser driver) {
        return analyticsService.getDriverHistory(driver.getId());
    }
}
