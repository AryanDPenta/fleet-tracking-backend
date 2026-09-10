package com.fleet.tracking.controller;

import com.fleet.tracking.dto.TripStatusEventRequest;
import com.fleet.tracking.entity.Driver;
import com.fleet.tracking.entity.DriverStatus;
import com.fleet.tracking.entity.Trip;
import com.fleet.tracking.entity.TripStatusEvent;
import com.fleet.tracking.entity.TripStatusEventType;
import com.fleet.tracking.repository.DriverRepository;
import com.fleet.tracking.repository.TripStatusEventRepository;
import com.fleet.tracking.security.AuthenticatedUser;
import com.fleet.tracking.service.AlertService;
import com.fleet.tracking.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/{tripId}/status-event")
@RequiredArgsConstructor
public class DriverStatusController {

    private final TripService tripService;
    private final TripStatusEventRepository tripStatusEventRepository;
    private final DriverRepository driverRepository;
    private final AlertService alertService;

    // Driver taps "Lunch", "Dinner", "Breakfast", "Stopping for another reason"
    // or "Resume" in the app. This logs the event (used later for threshold
    // calc), notifies the admin, AND persists whether the driver is currently
    // on a break onto Driver.status - that's what lets the frontend correctly
    // restore "on a break" state after a refresh or tab switch, instead of it
    // resetting to "driving" every time the page remounts.
    @PostMapping
    @Transactional
    public void reportStatus(@AuthenticationPrincipal AuthenticatedUser user,
                             @PathVariable Long tripId,
                             @Valid @RequestBody TripStatusEventRequest request) {
        Trip trip = tripService.getOwnedTrip(user.getId(), tripId);

        TripStatusEvent event = TripStatusEvent.builder()
                .trip(trip)
                .eventType(request.getEventType())
                .note(request.getNote())
                .build();
        tripStatusEventRepository.save(event);

        Driver driver = trip.getDriver();
        driver.setStatus(isBreakStart(request.getEventType()) ? DriverStatus.RESTING : DriverStatus.ON_TRIP);
        driverRepository.save(driver);

        alertService.raiseTripStatusAlert(trip, request.getEventType());
    }

    // BREAKFAST/LUNCH/DINNER/MANUAL_STOP all mean "driver has stopped and is
    // not currently driving" - only RESUMED means "back to driving."
    private boolean isBreakStart(TripStatusEventType type) {
        return type != TripStatusEventType.RESUMED;
    }
}
