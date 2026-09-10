package com.fleet.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TruckCreateRequest {
    @NotBlank
    private String registrationNumber;

    private String model;
}
