package com.fleet.tracking.repository;

import com.fleet.tracking.entity.LocationPing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LocationPingRepository extends JpaRepository<LocationPing, Long> {

    List<LocationPing> findByTripIdAndRecordedAtAfterOrderByRecordedAtAsc(Long tripId, LocalDateTime after);

    List<LocationPing> findByTripIdOrderByRecordedAtDesc(Long tripId);

    LocationPing findTopByTripIdOrderByRecordedAtDesc(Long tripId);

    // Bulk delete as a single SQL statement, not a derived deleteBy... method -
    // Spring Data would otherwise load every matching row into memory as an
    // entity before deleting each one individually, which doesn't scale once
    // this table has millions of rows. Returns the number of rows removed, for logging.
    @Modifying
    @Query("delete from LocationPing p where p.recordedAt < :cutoff")
    int deleteByRecordedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
