package com.fleet.tracking.repository;

import com.fleet.tracking.entity.DriverDaySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DriverDaySummaryRepository extends JpaRepository<DriverDaySummary, Long> {

    // driver is fetched eagerly - DriverDaySummaryDTO.from() reads driver.getName(),
    // which would otherwise be a lazy proxy read after this query's transaction closes.
    @Query("select s from DriverDaySummary s join fetch s.driver where s.driver.id = :driverId and s.summaryDate = :date")
    Optional<DriverDaySummary> findByDriverIdAndSummaryDate(@Param("driverId") Long driverId, @Param("date") LocalDate date);

    @Query("select s from DriverDaySummary s join fetch s.driver where s.driver.id = :driverId order by s.summaryDate desc")
    List<DriverDaySummary> findByDriverIdOrderBySummaryDateDesc(@Param("driverId") Long driverId);

    List<DriverDaySummary> findByThresholdMetFalseAndSummaryDate(LocalDate date);
}
