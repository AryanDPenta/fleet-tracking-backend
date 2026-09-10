package com.fleet.tracking.scheduler;

import com.fleet.tracking.entity.Trip;
import java.util.List;
import com.fleet.tracking.repository.CompanyRepository;
import com.fleet.tracking.service.IdleDetectionService;
import com.fleet.tracking.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Safety-net sweep across ALL ongoing trips, independent of individual pings.
// Runs on a fixed interval (see app.idle.check-interval-ms in application.yml).
@Component
@RequiredArgsConstructor
public class IdleCheckJob {

    private final CompanyRepository companyRepository;
    private final TripService tripService;
    private final IdleDetectionService idleDetectionService;

    @Scheduled(fixedRateString = "${app.idle.check-interval-ms}")
    public void runIdleSweep() {
        companyRepository.findAll().forEach(company -> {
            List<Trip> ongoing = tripService.getOngoingTripsForCompany(company.getId());
            ongoing.forEach(idleDetectionService::checkTripForIdle);
        });
    }
}
