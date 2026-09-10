package com.fleet.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String identifier; // phone for driver, email for admin/company

    @NotBlank
    private String password;
}
