package com.fleet.tracking.controller;

import com.fleet.tracking.dto.AlertDTO;
import com.fleet.tracking.dto.AlertMessageRequest;
import com.fleet.tracking.security.AuthenticatedUser;
import com.fleet.tracking.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // Admin sends a free-text alert/message to a specific driver (shows up on their app)
    @PostMapping("/send")
    public AlertDTO sendMessage(@AuthenticationPrincipal AuthenticatedUser admin,
                                 @Valid @RequestBody AlertMessageRequest request) {
        return alertService.sendAdminMessageToDriver(admin.getId(), request.getDriverId(), request.getMessage());
    }

    // Admin dashboard: idle alerts, threshold breaches, trip status updates
    @GetMapping("/admin")
    public List<AlertDTO> getAdminAlerts(@AuthenticationPrincipal AuthenticatedUser admin) {
        return alertService.getAlertsForAdmin(admin.getId());
    }

    // Driver app: admin messages + own threshold warnings
    @GetMapping("/driver")
    public List<AlertDTO> getDriverAlerts(@AuthenticationPrincipal AuthenticatedUser driver) {
        return alertService.getAlertsForDriver(driver.getId());
    }

    @PostMapping("/{alertId}/ack")
    public void acknowledge(@PathVariable Long alertId) {
        alertService.acknowledge(alertId);
    }
}
