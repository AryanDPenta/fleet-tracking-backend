package com.fleet.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverCreateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotBlank
    private String password;
}
