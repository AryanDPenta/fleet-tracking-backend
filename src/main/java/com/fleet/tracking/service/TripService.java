package com.fleet.tracking.service;

import com.fleet.tracking.dto.TripDTO;
import com.fleet.tracking.dto.TripStartRequest;
import com.fleet.tracking.entity.*;
import com.fleet.tracking.exception.ApiException;
import com.fleet.tracking.repository.DriverRepository;
import com.fleet.tracking.repository.TripRepository;
import com.fleet.tracking.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final TruckRepository truckRepository;

    @Transactional
    public TripDTO startTrip(Long driverId, TripStartRequest request) {
        // A driver can only have one active trip at a time
        tripRepository.findByDriverIdAndStatusIn(driverId, List.of(TripStatus.ONGOING, TripStatus.PAUSED))
                .ifPresent(t -> {
                    throw new ApiException("Driver already has an active trip", HttpStatus.CONFLICT);
                });

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ApiException("Driver not found", HttpStatus.NOT_FOUND));

        Truck truck = truckRepository.findById(request.getTruckId())
                .orElseThrow(() -> new ApiException("Truck not found", HttpStatus.NOT_FOUND));

        Trip trip = Trip.builder()
                .driver(driver)
                .truck(truck)
                .company(driver.getCompany())
                .sourceLocation(request.getSourceLocation())
                .destinationLocation(request.getDestinationLocation())
                .status(TripStatus.ONGOING)
                .startTime(LocalDateTime.now())
                .thresholdMinutesOverride(request.getThresholdMinutesOverride())
                .build();

        trip = tripRepository.save(trip);

        driver.setStatus(DriverStatus.ON_TRIP);
        driverRepository.save(driver);

        return TripDTO.from(trip);
    }

    @Transactional
    public TripDTO endTrip(Long driverId, Long tripId) {
        Trip trip = getOwnedTrip(driverId, tripId);
        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndTime(LocalDateTime.now());
        trip = tripRepository.save(trip);

        Driver driver = trip.getDriver();
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);

        return TripDTO.from(trip);
    }

    public TripDTO getActiveTrip(Long driverId) {
        Trip trip = tripRepository
                .findByDriverIdAndStatusIn(driverId, List.of(TripStatus.ONGOING, TripStatus.PAUSED))
                .orElseThrow(() -> new ApiException("No active trip", HttpStatus.NOT_FOUND));
        return TripDTO.from(trip);
    }

    public Trip getOwnedTrip(Long driverId, Long tripId) {
        Trip trip = tripRepository.findWithDriverAndTruckById(tripId)
                .orElseThrow(() -> new ApiException("Trip not found", HttpStatus.NOT_FOUND));
        if (!trip.getDriver().getId().equals(driverId)) {
            throw new ApiException("Trip does not belong to this driver", HttpStatus.FORBIDDEN);
        }
        return trip;
    }

    public List<Trip> getOngoingTripsForCompany(Long companyId) {
        return tripRepository.findByCompanyIdAndStatusIn(companyId, List.of(TripStatus.ONGOING, TripStatus.PAUSED));
    }

    public List<Trip> getAllTripsForCompany(Long companyId) {
        return tripRepository.findByCompanyId(companyId);
    }
}
