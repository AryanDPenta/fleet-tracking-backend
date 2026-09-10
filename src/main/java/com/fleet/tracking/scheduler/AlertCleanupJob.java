package com.fleet.tracking.scheduler;

import com.fleet.tracking.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Alerts grow far slower than location_pings (one row per real event, not
// one every 15s), so this is a lower urgency cleanup - but still unbounded
// otherwise. Only ever deletes ACKNOWLEDGED alerts past the retention window;
// anything still unacknowledged is kept indefinitely regardless of age, since
// it may still represent something nobody has acted on yet.
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertCleanupJob {

    private final AlertRepository alertRepository;

    @Value("${app.alert.retention-days}")
    private int retentionDays;

    @Scheduled(cron = "${app.alert.cleanup-cron}")
    @Transactional
    public void purgeOldAcknowledgedAlerts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = alertRepository.deleteByAcknowledgedTrueAndCreatedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Purged {} acknowledged alerts older than {} days (before {})", deleted, retentionDays, cutoff);
        }
    }
}
