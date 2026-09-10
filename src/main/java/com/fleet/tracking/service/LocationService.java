package com.fleet.tracking.service;

import com.fleet.tracking.dto.LiveTruckDTO;
import com.fleet.tracking.dto.LocationPingRequest;
import com.fleet.tracking.entity.LocationPing;
import com.fleet.tracking.entity.Trip;
import com.fleet.tracking.entity.TripStatus;
import com.fleet.tracking.exception.ApiException;
import com.fleet.tracking.repository.LocationPingRepository;
import com.fleet.tracking.repository.TripRepository;
import com.fleet.tracking.websocket.LocationBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationPingRepository locationPingRepository;
    private final TripRepository tripRepository;
    private final LocationBroadcaster broadcaster;
    private final IdleDetectionService idleDetectionService;

    @Transactional
    public void recordPing(Long driverId, LocationPingRequest request) {
        Trip trip = tripRepository.findWithDriverAndTruckById(request.getTripId())
                .orElseThrow(() -> new ApiException("Trip not found", HttpStatus.NOT_FOUND));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new ApiException("Trip does not belong to this driver", HttpStatus.FORBIDDEN);
        }
        if (trip.getStatus() != TripStatus.ONGOING && trip.getStatus() != TripStatus.PAUSED) {
            throw new ApiException("Trip is not active", HttpStatus.CONFLICT);
        }

        LocationPing ping = LocationPing.builder()
                .trip(trip)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speedKmh(request.getSpeedKmh())
                .build();
        ping = locationPingRepository.save(ping);

        LiveTruckDTO dto = new LiveTruckDTO(
                trip.getId(),
                trip.getTruck().getId(),
                trip.getTruck().getRegistrationNumber(),
                trip.getDriver().getId(),
                trip.getDriver().getName(),
                trip.getDriver().getStatus().name(),
                ping.getLatitude(),
                ping.getLongitude(),
                ping.getRecordedAt()
        );
        broadcaster.broadcastLocation(trip.getCompany().getId(), dto);

        // Quick inline check on every ping; the scheduled IdleCheckJob is the safety net
        // in case pings stop arriving altogether (phone off, no signal, etc).
        idleDetectionService.checkTripForIdle(trip);
    }
}
