package com.fleet.tracking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRegisterRequest {
    @NotBlank
    private String companyName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
