package com.fleet.tracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationPingRequest {
    @NotNull
    private Long tripId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private Double speedKmh;
}
