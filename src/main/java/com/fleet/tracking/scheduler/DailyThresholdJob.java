package com.fleet.tracking.scheduler;

import com.fleet.tracking.entity.Driver;
import com.fleet.tracking.repository.DriverRepository;
import com.fleet.tracking.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Runs once a day (default: just after midnight) and computes every driver's
// drive-time-vs-threshold summary for the day that just ended.
@Component
@RequiredArgsConstructor
public class DailyThresholdJob {

    private final DriverRepository driverRepository;
    private final ThresholdService thresholdService;

    @Scheduled(cron = "0 5 0 * * *") // 00:05 every day
    public void runDailyThresholdCheck() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        for (Driver driver : driverRepository.findAll()) {
            thresholdService.computeAndStoreDailySummary(driver, yesterday);
        }
    }
}
