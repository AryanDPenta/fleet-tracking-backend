package com.fleet.tracking.dto;

import com.fleet.tracking.entity.DriverDaySummary;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DriverDaySummaryDTO {
    private Long driverId;
    private String driverName;
    private LocalDate summaryDate;
    private long totalDriveMinutes;
    private long thresholdMinutes;
    private boolean thresholdMet;
    private long carryOverShortfallMinutes;

    public static DriverDaySummaryDTO from(DriverDaySummary s) {
        return new DriverDaySummaryDTO(
                s.getDriver().getId(),
                s.getDriver().getName(),
                s.getSummaryDate(),
                s.getTotalDriveMinutes(),
                s.getThresholdMinutes(),
                s.isThresholdMet(),
                s.getCarryOverShortfallMinutes()
        );
    }
}
