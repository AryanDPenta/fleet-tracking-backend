package com.fleet.tracking.dto;

import com.fleet.tracking.entity.Trip;
import com.fleet.tracking.entity.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TripDTO {
    private Long id;
    private Long driverId;
    private String driverName;
    private Long truckId;
    private String truckRegNumber;
    private String sourceLocation;
    private String destinationLocation;
    private TripStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    // Reflects Driver.status (AVAILABLE/ON_TRIP/RESTING) - the frontend uses this
    // to correctly show "on a break" vs "driving" after a page refresh or tab
    // switch, instead of guessing from local component state that resets on remount.
    private String driverStatus;

    public static TripDTO from(Trip t) {
        return new TripDTO(
                t.getId(),
                t.getDriver().getId(),
                t.getDriver().getName(),
                t.getTruck().getId(),
                t.getTruck().getRegistrationNumber(),
                t.getSourceLocation(),
                t.getDestinationLocation(),
                t.getStatus(),
                t.getStartTime(),
                t.getEndTime(),
                t.getDriver().getStatus().name()
        );
    }
}
