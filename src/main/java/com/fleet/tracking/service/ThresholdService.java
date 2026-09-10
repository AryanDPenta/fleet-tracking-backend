package com.fleet.tracking.service;

import com.fleet.tracking.entity.*;
import com.fleet.tracking.repository.DriverDaySummaryRepository;
import com.fleet.tracking.repository.TripRepository;
import com.fleet.tracking.repository.TripStatusEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

// Computes each driver's total drive time for a given day (gross trip time minus
// declared breaks), compares it to the daily threshold, and carries forward any
// shortfall until a day meets the threshold again.
@Service
@RequiredArgsConstructor
public class ThresholdService {

    private final TripRepository tripRepository;
    private final TripStatusEventRepository tripStatusEventRepository;
    private final DriverDaySummaryRepository driverDaySummaryRepository;
    private final AlertService alertService;

    @Value("${app.threshold.daily-drive-minutes}")
    private long defaultThresholdMinutes;

    private static final Set<TripStatusEventType> BREAK_START_TYPES = Set.of(
            TripStatusEventType.BREAKFAST, TripStatusEventType.LUNCH, TripStatusEventType.DINNER);

    @Transactional
    public DriverDaySummary computeAndStoreDailySummary(Driver driver, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<Trip> trips = tripRepository.findByDriverIdAndStartTimeBetween(driver.getId(), dayStart, dayEnd);

        long totalDriveMinutes = 0;
        for (Trip trip : trips) {
            LocalDateTime start = trip.getStartTime().isBefore(dayStart) ? dayStart : trip.getStartTime();
            LocalDateTime end = trip.getEndTime() == null
                    ? LocalDateTime.now()
                    : (trip.getEndTime().isAfter(dayEnd) ? dayEnd : trip.getEndTime());

            long grossMinutes = ChronoUnit.MINUTES.between(start, end);
            long breakMinutes = computeBreakMinutes(trip, start, end);
            totalDriveMinutes += Math.max(0, grossMinutes - breakMinutes);
        }

        long threshold = trips.stream()
                .map(Trip::getThresholdMinutesOverride)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse((int) defaultThresholdMinutes);

        boolean thresholdMet = totalDriveMinutes >= threshold;

        long previousCarry = driverDaySummaryRepository
                .findByDriverIdAndSummaryDate(driver.getId(), date.minusDays(1))
                .map(prev -> prev.isThresholdMet() ? 0 : prev.getCarryOverShortfallMinutes()
                        + Math.max(0, prev.getThresholdMinutes() - prev.getTotalDriveMinutes()))
                .orElse(0L);

        long todayShortfall = thresholdMet ? 0 : (threshold - totalDriveMinutes);
        long carryOverShortfallMinutes = previousCarry + todayShortfall;

        DriverDaySummary summary = driverDaySummaryRepository
                .findByDriverIdAndSummaryDate(driver.getId(), date)
                .orElse(DriverDaySummary.builder().driver(driver).summaryDate(date).build());

        summary.setTotalDriveMinutes(totalDriveMinutes);
        summary.setThresholdMinutes(threshold);
        summary.setThresholdMet(thresholdMet);
        summary.setCarryOverShortfallMinutes(carryOverShortfallMinutes);

        summary = driverDaySummaryRepository.save(summary);

        if (!thresholdMet) {
            alertService.raiseThresholdAlert(summary);
        }

        return summary;
    }

    // Sums minutes spent in declared breaks (BREAKFAST/LUNCH/DINNER -> RESUMED/MANUAL_STOP)
    // that fall within [windowStart, windowEnd].
    private long computeBreakMinutes(Trip trip, LocalDateTime windowStart, LocalDateTime windowEnd) {
        List<TripStatusEvent> events = tripStatusEventRepository
                .findByTripIdOrderByEventTimeAsc(trip.getId());

        long breakMinutes = 0;
        LocalDateTime breakStart = null;

        for (TripStatusEvent event : events) {
            if (BREAK_START_TYPES.contains(event.getEventType())) {
                if (breakStart == null) {
                    breakStart = event.getEventTime();
                }
            } else if (breakStart != null) {
                // RESUMED or MANUAL_STOP closes the break
                breakMinutes += overlapMinutes(breakStart, event.getEventTime(), windowStart, windowEnd);
                breakStart = null;
            }
        }

        // Break still open (no RESUMED event yet) - count up to window end
        if (breakStart != null) {
            breakMinutes += overlapMinutes(breakStart, windowEnd, windowStart, windowEnd);
        }

        return breakMinutes;
    }

    private long overlapMinutes(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        LocalDateTime start = aStart.isAfter(bStart) ? aStart : bStart;
        LocalDateTime end = aEnd.isBefore(bEnd) ? aEnd : bEnd;
        return start.isBefore(end) ? ChronoUnit.MINUTES.between(start, end) : 0;
    }
}
