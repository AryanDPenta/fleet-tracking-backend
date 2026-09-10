package com.fleet.tracking.service;

import com.fleet.tracking.dto.CompanyRegisterRequest;
import com.fleet.tracking.dto.LoginRequest;
import com.fleet.tracking.dto.LoginResponse;
import com.fleet.tracking.entity.Company;
import com.fleet.tracking.entity.Driver;
import com.fleet.tracking.entity.Role;
import com.fleet.tracking.exception.ApiException;
import com.fleet.tracking.repository.CompanyRepository;
import com.fleet.tracking.repository.DriverRepository;
import com.fleet.tracking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CompanyRepository companyRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Tries company (admin) login by email first, then driver login by phone.
    // Keeps one login endpoint for both roles as required by the single React app.
    public LoginResponse login(LoginRequest request) {
        var companyOpt = companyRepository.findByEmail(request.getIdentifier());
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            if (passwordEncoder.matches(request.getPassword(), company.getPasswordHash())) {
                String token = jwtUtil.generateToken(company.getId(), Role.ADMIN, company.getCompanyName());
                return new LoginResponse(token, Role.ADMIN, company.getId(), company.getCompanyName());
            }
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        var driverOpt = driverRepository.findByPhone(request.getIdentifier());
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            if (passwordEncoder.matches(request.getPassword(), driver.getPasswordHash())) {
                String token = jwtUtil.generateToken(driver.getId(), Role.DRIVER, driver.getName());
                return new LoginResponse(token, Role.DRIVER, driver.getId(), driver.getName());
            }
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        throw new ApiException("No account found for that identifier", HttpStatus.UNAUTHORIZED);
    }

    // Onboards a new company/admin account. In production you'd likely gate this
    // behind an invite code or manual approval rather than leaving it fully open.
    public LoginResponse registerCompany(CompanyRegisterRequest request) {
        companyRepository.findByEmail(request.getEmail()).ifPresent(c -> {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        });

        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        company = companyRepository.save(company);

        String token = jwtUtil.generateToken(company.getId(), Role.ADMIN, company.getCompanyName());
        return new LoginResponse(token, Role.ADMIN, company.getId(), company.getCompanyName());
    }
}
