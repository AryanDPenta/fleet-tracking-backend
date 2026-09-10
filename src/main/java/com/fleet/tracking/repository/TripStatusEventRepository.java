package com.fleet.tracking.repository;

import com.fleet.tracking.entity.TripStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TripStatusEventRepository extends JpaRepository<TripStatusEvent, Long> {
    List<TripStatusEvent> findByTripIdOrderByEventTimeAsc(Long tripId);
    List<TripStatusEvent> findByTripIdAndEventTimeBetween(Long tripId, LocalDateTime from, LocalDateTime to);
}
