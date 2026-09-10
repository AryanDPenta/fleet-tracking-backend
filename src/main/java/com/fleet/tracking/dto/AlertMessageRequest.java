package com.fleet.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertMessageRequest {
    @NotNull
    private Long driverId;

    @NotBlank
    private String message;
}
