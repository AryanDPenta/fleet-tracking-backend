package com.fleet.tracking.controller;

import com.fleet.tracking.dto.TripDTO;
import com.fleet.tracking.dto.TripStartRequest;
import com.fleet.tracking.security.AuthenticatedUser;
import com.fleet.tracking.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping("/start")
    public TripDTO startTrip(@AuthenticationPrincipal AuthenticatedUser user,
                              @Valid @RequestBody TripStartRequest request) {
        return tripService.startTrip(user.getId(), request);
    }

    @PostMapping("/{tripId}/end")
    public TripDTO endTrip(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long tripId) {
        return tripService.endTrip(user.getId(), tripId);
    }

    @GetMapping("/active")
    public TripDTO getActiveTrip(@AuthenticationPrincipal AuthenticatedUser user) {
        return tripService.getActiveTrip(user.getId());
    }
}
