package com.fleet.tracking.repository;

import com.fleet.tracking.entity.Trip;
import com.fleet.tracking.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // These fetch driver+truck eagerly because callers (controllers, the
    // scheduled idle-check job) read trip.getDriver().getName() /
    // trip.getTruck().getRegistrationNumber() etc. AFTER this query's own
    // transaction has closed - without the fetch join those are lazy proxies
    // and throw LazyInitializationException the moment a non-ID field is read.
    @Query("select t from Trip t join fetch t.driver join fetch t.truck where t.driver.id = :driverId and t.status in :statuses")
    Optional<Trip> findByDriverIdAndStatusIn(@Param("driverId") Long driverId, @Param("statuses") List<TripStatus> statuses);

    @Query("select t from Trip t join fetch t.driver join fetch t.truck where t.company.id = :companyId and t.status = :status")
    List<Trip> findByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") TripStatus status);

    @Query("select t from Trip t join fetch t.driver join fetch t.truck where t.company.id = :companyId and t.status in :statuses")
    List<Trip> findByCompanyIdAndStatusIn(@Param("companyId") Long companyId, @Param("statuses") List<TripStatus> statuses);

    // Only used internally against trip's own columns (start/end time, threshold
    // override) - no driver/truck navigation, so no fetch join needed here.
    List<Trip> findByDriverIdAndStartTimeBetween(Long driverId, LocalDateTime from, LocalDateTime to);

    @Query("select t from Trip t join fetch t.driver join fetch t.truck where t.company.id = :companyId")
    List<Trip> findByCompanyId(@Param("companyId") Long companyId);

    // Used wherever a single trip is fetched and its driver/truck fields get read
    // later in the call chain (e.g. building an alert message) - a plain findById()
    // here would hand back a Trip whose driver/truck are unfetched lazy proxies,
    // which blow up the moment something outside the original session reads them.
    @Query("select t from Trip t join fetch t.driver join fetch t.truck where t.id = :id")
    Optional<Trip> findWithDriverAndTruckById(@Param("id") Long id);
}
