package com.fleet.tracking.service;

import com.fleet.tracking.entity.AlertType;
import com.fleet.tracking.entity.LocationPing;
import com.fleet.tracking.entity.Trip;
import com.fleet.tracking.repository.LocationPingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Core "is this truck resting?" logic: same location (within a small radius)
// for >= idle-threshold-minutes counts as idle/resting.
@Service
@RequiredArgsConstructor
public class IdleDetectionService {

    private final LocationPingRepository locationPingRepository;
    private final AlertService alertService;

    @Value("${app.idle.radius-meters}")
    private double radiusMeters;

    @Value("${app.idle.idle-threshold-minutes}")
    private long idleThresholdMinutes;

    public void checkTripForIdle(Trip trip) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(idleThresholdMinutes);

        List<LocationPing> pings = locationPingRepository
                .findByTripIdAndRecordedAtAfterOrderByRecordedAtAsc(trip.getId(), windowStart);

        if (pings.size() < 2) {
            return; // not enough data yet to judge idleness
        }

        LocationPing reference = pings.get(0);
        boolean allWithinRadius = pings.stream()
                .allMatch(p -> distanceMeters(reference.getLatitude(), reference.getLongitude(),
                        p.getLatitude(), p.getLongitude()) <= radiusMeters);

        long spanMinutes = ChronoUnit.MINUTES.between(reference.getRecordedAt(),
                pings.get(pings.size() - 1).getRecordedAt());

        if (allWithinRadius && spanMinutes >= idleThresholdMinutes) {
            // Avoid spamming: only raise a fresh alert if we haven't already alerted
            // for this trip within the current idle window.
            boolean alreadyAlerted = alertService.hasRecentAlert(
                    trip.getDriver().getId(), AlertType.IDLE_DETECTED, windowStart);

            if (!alreadyAlerted) {
                alertService.raiseIdleAlert(trip, spanMinutes);
            }
        }
    }

    // Haversine formula - distance between two lat/lng points in meters
    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
