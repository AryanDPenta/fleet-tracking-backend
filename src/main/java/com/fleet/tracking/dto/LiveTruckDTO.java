package com.fleet.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LiveTruckDTO {
    private Long tripId;
    private Long truckId;
    private String truckRegNumber;
    private Long driverId;
    private String driverName;
    private String driverStatus; // DRIVING / RESTING / ON_BREAK
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdated;
}
