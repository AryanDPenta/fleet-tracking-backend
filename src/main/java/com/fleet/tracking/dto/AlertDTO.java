package com.fleet.tracking.dto;

import com.fleet.tracking.entity.Alert;
import com.fleet.tracking.entity.AlertType;
import com.fleet.tracking.entity.TargetRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AlertDTO {
    private Long id;
    private AlertType alertType;
    private TargetRole targetRole;
    private String message;
    private boolean acknowledged;
    private LocalDateTime createdAt;
    private Long driverId;
    private String driverName;
    private Long tripId;

    public static AlertDTO from(Alert a) {
        return new AlertDTO(
                a.getId(),
                a.getAlertType(),
                a.getTargetRole(),
                a.getMessage(),
                a.isAcknowledged(),
                a.getCreatedAt(),
                a.getDriver() != null ? a.getDriver().getId() : null,
                a.getDriver() != null ? a.getDriver().getName() : null,
                a.getTrip() != null ? a.getTrip().getId() : null
        );
    }
}
