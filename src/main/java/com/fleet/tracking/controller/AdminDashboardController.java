package com.fleet.tracking.controller;

import com.fleet.tracking.dto.DriverCreateRequest;
import com.fleet.tracking.dto.DriverDTO;
import com.fleet.tracking.dto.LiveTruckDTO;
import com.fleet.tracking.dto.TripDTO;
import com.fleet.tracking.dto.TruckCreateRequest;
import com.fleet.tracking.dto.TruckDTO;
import com.fleet.tracking.entity.Company;
import com.fleet.tracking.entity.Driver;
import com.fleet.tracking.entity.LocationPing;
import com.fleet.tracking.entity.Trip;
import com.fleet.tracking.entity.Truck;
import com.fleet.tracking.exception.ApiException;
import com.fleet.tracking.repository.CompanyRepository;
import com.fleet.tracking.repository.DriverRepository;
import com.fleet.tracking.repository.LocationPingRepository;
import com.fleet.tracking.repository.TruckRepository;
import com.fleet.tracking.security.AuthenticatedUser;
import com.fleet.tracking.service.AnalyticsService;
import com.fleet.tracking.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final TripService tripService;
    private final DriverRepository driverRepository;
    private final LocationPingRepository locationPingRepository;
    private final AnalyticsService analyticsService;
    private final TruckRepository truckRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    // Powers the live map: latest known position of every truck currently on a trip.
    @GetMapping("/live-trucks")
    public List<LiveTruckDTO> getLiveTrucks(@AuthenticationPrincipal AuthenticatedUser admin) {
        List<Trip> ongoing = tripService.getOngoingTripsForCompany(admin.getId());

        return ongoing.stream().map(trip -> {
            LocationPing latest = locationPingRepository.findTopByTripIdOrderByRecordedAtDesc(trip.getId());
            return new LiveTruckDTO(
                    trip.getId(),
                    trip.getTruck().getId(),
                    trip.getTruck().getRegistrationNumber(),
                    trip.getDriver().getId(),
                    trip.getDriver().getName(),
                    trip.getDriver().getStatus().name(),
                    latest != null ? latest.getLatitude() : null,
                    latest != null ? latest.getLongitude() : null,
                    latest != null ? latest.getRecordedAt() : null
            );
        }).collect(Collectors.toList());
    }

    @GetMapping("/drivers")
    public List<DriverDTO> getDrivers(@AuthenticationPrincipal AuthenticatedUser admin) {
        return driverRepository.findByCompanyId(admin.getId())
                .stream().map(DriverDTO::from).collect(Collectors.toList());
    }

    @GetMapping("/trips")
    public List<TripDTO> getAllTrips(@AuthenticationPrincipal AuthenticatedUser admin) {
        return tripService.getAllTripsForCompany(admin.getId())
                .stream().map(TripDTO::from).collect(Collectors.toList());
    }

    @GetMapping("/analytics")
    public com.fleet.tracking.dto.AnalyticsSummaryDTO getAnalytics(@AuthenticationPrincipal AuthenticatedUser admin) {
        return analyticsService.getCompanySummary(admin.getId());
    }

    // Admin onboards a driver under their company. Driver then logs in with phone+password.
    @PostMapping("/drivers")
    public DriverDTO createDriver(@AuthenticationPrincipal AuthenticatedUser admin,
                                @Valid @RequestBody DriverCreateRequest request) {
        Company company = companyRepository.findById(admin.getId())
                .orElseThrow(() -> new ApiException("Company not found", HttpStatus.NOT_FOUND));

        Driver driver = Driver.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .company(company)
                .build();
        return DriverDTO.from(driverRepository.save(driver));
    }

    @PostMapping("/trucks")
    public TruckDTO createTruck(@AuthenticationPrincipal AuthenticatedUser admin,
                              @Valid @RequestBody TruckCreateRequest request) {
        Company company = companyRepository.findById(admin.getId())
                .orElseThrow(() -> new ApiException("Company not found", HttpStatus.NOT_FOUND));

        Truck truck = Truck.builder()
                .registrationNumber(request.getRegistrationNumber())
                .model(request.getModel())
                .company(company)
                .active(true)
                .build();
        return TruckDTO.from(truckRepository.save(truck));
    }
}
