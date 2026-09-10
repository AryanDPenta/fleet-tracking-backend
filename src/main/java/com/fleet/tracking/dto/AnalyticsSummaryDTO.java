package com.fleet.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyticsSummaryDTO {
    private long totalTrips;
    private long completedTrips;
    private long ongoingTrips;
    private double averageDriveMinutesPerDay;
    private long driversBelowThresholdToday;
    private long totalDrivers;
    private long totalTrucks;
}
