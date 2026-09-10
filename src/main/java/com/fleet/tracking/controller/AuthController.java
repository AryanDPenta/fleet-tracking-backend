package com.fleet.tracking.controller;

import com.fleet.tracking.dto.CompanyRegisterRequest;
import com.fleet.tracking.dto.LoginRequest;
import com.fleet.tracking.dto.LoginResponse;
import com.fleet.tracking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Single endpoint for both driver (phone) and admin/company (email) login.
    // The returned JWT carries the role, so the React app can route accordingly.
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Company/admin sign-up. Drivers are created by an already-logged-in admin
    // via POST /api/admin/drivers, not self-registered.
    @PostMapping("/register-company")
    public LoginResponse registerCompany(@Valid @RequestBody CompanyRegisterRequest request) {
        return authService.registerCompany(request);
    }
}
