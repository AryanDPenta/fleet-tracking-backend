package com.fleet.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripStartRequest {
    @NotNull
    private Long truckId;

    @NotBlank
    private String sourceLocation;

    @NotBlank
    private String destinationLocation;

    // optional override, minutes
    private Integer thresholdMinutesOverride;
}
