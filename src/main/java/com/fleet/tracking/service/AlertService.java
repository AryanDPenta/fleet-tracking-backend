package com.fleet.tracking.service;

import com.fleet.tracking.dto.AlertDTO;
import com.fleet.tracking.entity.*;
import com.fleet.tracking.exception.ApiException;
import com.fleet.tracking.repository.AlertRepository;
import com.fleet.tracking.repository.DriverRepository;
import com.fleet.tracking.websocket.LocationBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final DriverRepository driverRepository;
    private final LocationBroadcaster broadcaster;

    public boolean hasRecentAlert(Long driverId, AlertType type, LocalDateTime since) {
        return !alertRepository.findByDriverIdAndAlertTypeAndCreatedAtAfter(driverId, type, since).isEmpty();
    }

    @Transactional
    public void raiseIdleAlert(Trip trip, long idleMinutes) {
        String message = String.format(
                "Truck %s (driver %s) hasn't moved in %d minutes on trip %s -> %s.",
                trip.getTruck().getRegistrationNumber(), trip.getDriver().getName(),
                idleMinutes, trip.getSourceLocation(), trip.getDestinationLocation());

        Alert alert = Alert.builder()
                .company(trip.getCompany())
                .driver(trip.getDriver())
                .trip(trip)
                .alertType(AlertType.IDLE_DETECTED)
                .targetRole(TargetRole.ADMIN)
                .message(message)
                .build();
        alert = alertRepository.save(alert);

        broadcaster.broadcastAlertToAdmin(trip.getCompany().getId(), AlertDTO.from(alert));
    }

    @Transactional
    public void raiseTripStatusAlert(Trip trip, TripStatusEventType eventType) {
        String message = String.format("%s reported %s during trip %s -> %s.",
                trip.getDriver().getName(), eventType, trip.getSourceLocation(), trip.getDestinationLocation());

        Alert alert = Alert.builder()
                .company(trip.getCompany())
                .driver(trip.getDriver())
                .trip(trip)
                .alertType(AlertType.TRIP_STATUS_UPDATE)
                .targetRole(TargetRole.ADMIN)
                .message(message)
                .build();
        alert = alertRepository.save(alert);

        broadcaster.broadcastAlertToAdmin(trip.getCompany().getId(), AlertDTO.from(alert));
    }

    @Transactional
    public void raiseThresholdAlert(DriverDaySummary summary) {
        Driver driver = summary.getDriver();
        String message = String.format(
                "%s drove %d min on %s, below the %d min threshold (shortfall carried: %d min).",
                driver.getName(), summary.getTotalDriveMinutes(), summary.getSummaryDate(),
                summary.getThresholdMinutes(), summary.getCarryOverShortfallMinutes());

        Alert adminAlert = Alert.builder()
                .company(driver.getCompany())
                .driver(driver)
                .alertType(AlertType.THRESHOLD_BREACH)
                .targetRole(TargetRole.ADMIN)
                .message(message)
                .build();
        adminAlert = alertRepository.save(adminAlert);
        broadcaster.broadcastAlertToAdmin(driver.getCompany().getId(), AlertDTO.from(adminAlert));

        Alert driverAlert = Alert.builder()
                .company(driver.getCompany())
                .driver(driver)
                .alertType(AlertType.THRESHOLD_BREACH)
                .targetRole(TargetRole.DRIVER)
                .message("You were under your daily driving threshold on " + summary.getSummaryDate()
                        + ". Shortfall carried forward: " + summary.getCarryOverShortfallMinutes() + " min.")
                .build();
        driverAlert = alertRepository.save(driverAlert);
        broadcaster.broadcastAlertToDriver(driver.getId(), AlertDTO.from(driverAlert));
    }

    @Transactional
    public AlertDTO sendAdminMessageToDriver(Long companyId, Long driverId, String message) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ApiException("Driver not found", HttpStatus.NOT_FOUND));

        if (!driver.getCompany().getId().equals(companyId)) {
            throw new ApiException("Driver does not belong to this company", HttpStatus.FORBIDDEN);
        }

        Alert alert = Alert.builder()
                .company(driver.getCompany())
                .driver(driver)
                .alertType(AlertType.ADMIN_MESSAGE)
                .targetRole(TargetRole.DRIVER)
                .message(message)
                .build();
        alert = alertRepository.save(alert);

        broadcaster.broadcastAlertToDriver(driver.getId(), AlertDTO.from(alert));
        return AlertDTO.from(alert);
    }

    public List<AlertDTO> getAlertsForAdmin(Long companyId) {
        return alertRepository.findByCompanyIdAndTargetRoleOrderByCreatedAtDesc(companyId, TargetRole.ADMIN)
                .stream().map(AlertDTO::from).collect(Collectors.toList());
    }

    public List<AlertDTO> getAlertsForDriver(Long driverId) {
        return alertRepository.findByDriverIdAndTargetRoleOrderByCreatedAtDesc(driverId, TargetRole.DRIVER)
                .stream().map(AlertDTO::from).collect(Collectors.toList());
    }

    @Transactional
    public void acknowledge(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ApiException("Alert not found", HttpStatus.NOT_FOUND));
        alert.setAcknowledged(true);
        alertRepository.save(alert);
    }
}
