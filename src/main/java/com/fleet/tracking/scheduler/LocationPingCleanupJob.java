package com.fleet.tracking.scheduler;

import com.fleet.tracking.repository.LocationPingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Keeps location_pings bounded instead of growing forever. Raw pings are only
// ever queried for a short recent window (idle-detection looks back at most
// app.idle.idle-threshold-minutes, the live map only cares about the latest
// one per trip) - nothing in the app needs pings from weeks ago, so there's
// no reason to keep them. Completed trips' totals live on in DriverDaySummary,
// which this cleanup never touches.
@Component
@RequiredArgsConstructor
@Slf4j
public class LocationPingCleanupJob {

    private final LocationPingRepository locationPingRepository;

    @Value("${app.location-ping.retention-days}")
    private int retentionDays;

    @Scheduled(cron = "${app.location-ping.cleanup-cron}")
    @Transactional
    public void purgeOldPings() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = locationPingRepository.deleteByRecordedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Purged {} location pings older than {} days (before {})", deleted, retentionDays, cutoff);
        }
    }
}
