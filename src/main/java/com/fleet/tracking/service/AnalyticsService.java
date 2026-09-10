package com.fleet.tracking.service;

import com.fleet.tracking.dto.AnalyticsSummaryDTO;
import com.fleet.tracking.dto.DriverDaySummaryDTO;
import com.fleet.tracking.entity.Driver;
import com.fleet.tracking.entity.Trip;
import com.fleet.tracking.entity.TripStatus;
import com.fleet.tracking.repository.DriverDaySummaryRepository;
import com.fleet.tracking.repository.DriverRepository;
import com.fleet.tracking.repository.TripRepository;
import com.fleet.tracking.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final TruckRepository truckRepository;
    private final DriverDaySummaryRepository driverDaySummaryRepository;

    public AnalyticsSummaryDTO getCompanySummary(Long companyId) {
        List<Trip> trips = tripRepository.findByCompanyId(companyId);
        long total = trips.size();
        long completed = trips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count();
        long ongoing = trips.stream()
                .filter(t -> t.getStatus() == TripStatus.ONGOING || t.getStatus() == TripStatus.PAUSED)
                .count();

        List<Driver> drivers = driverRepository.findByCompanyId(companyId);
        long totalTrucks = truckRepository.findByCompanyIdAndActiveTrue(companyId).size();

        LocalDate today = LocalDate.now();
        long belowThresholdToday = drivers.stream()
                .flatMap(d -> driverDaySummaryRepository.findByDriverIdAndSummaryDate(d.getId(), today).stream())
                .filter(s -> !s.isThresholdMet())
                .count();

        double avgDriveMinutes = drivers.stream()
                .flatMap(d -> driverDaySummaryRepository.findByDriverIdOrderBySummaryDateDesc(d.getId()).stream())
                .mapToLong(s -> s.getTotalDriveMinutes())
                .average()
                .orElse(0.0);

        return new AnalyticsSummaryDTO(total, completed, ongoing, avgDriveMinutes,
                belowThresholdToday, drivers.size(), totalTrucks);
    }

    public List<DriverDaySummaryDTO> getDriverHistory(Long driverId) {
        return driverDaySummaryRepository.findByDriverIdOrderBySummaryDateDesc(driverId)
                .stream().map(DriverDaySummaryDTO::from).collect(Collectors.toList());
    }
}
