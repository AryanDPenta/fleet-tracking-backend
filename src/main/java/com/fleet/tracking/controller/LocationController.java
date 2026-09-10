package com.fleet.tracking.controller;

import com.fleet.tracking.dto.LocationPingRequest;
import com.fleet.tracking.security.AuthenticatedUser;
import com.fleet.tracking.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // Driver app calls this every few seconds via navigator.geolocation.watchPosition.
    // Also broadcasts to the admin live map over WebSocket - see LocationBroadcaster.
    @PostMapping("/ping")
    public void ping(@AuthenticationPrincipal AuthenticatedUser user,
                      @Valid @RequestBody LocationPingRequest request) {
        locationService.recordPing(user.getId(), request);
    }
}
