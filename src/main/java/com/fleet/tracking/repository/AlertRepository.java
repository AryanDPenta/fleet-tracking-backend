package com.fleet.tracking.repository;

import com.fleet.tracking.entity.Alert;
import com.fleet.tracking.entity.AlertType;
import com.fleet.tracking.entity.TargetRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    // left join fetch (not inner join fetch) because driver/trip are nullable
    // columns on Alert - an inner join fetch would silently drop rows where
    // either is null. AlertDTO.from() reads driver.getName() and trip.getId(),
    // which is what needs the eager fetch in the first place.
    @Query("select a from Alert a left join fetch a.driver left join fetch a.trip " +
            "where a.company.id = :companyId and a.targetRole = :targetRole order by a.createdAt desc")
    List<Alert> findByCompanyIdAndTargetRoleOrderByCreatedAtDesc(
            @Param("companyId") Long companyId, @Param("targetRole") TargetRole targetRole);

    @Query("select a from Alert a left join fetch a.driver left join fetch a.trip " +
            "where a.driver.id = :driverId and a.targetRole = :targetRole order by a.createdAt desc")
    List<Alert> findByDriverIdAndTargetRoleOrderByCreatedAtDesc(
            @Param("driverId") Long driverId, @Param("targetRole") TargetRole targetRole);

    // Only used for an existence check (hasRecentAlert) - no DTO mapping, so no fetch join needed.
    List<Alert> findByDriverIdAndAlertTypeAndCreatedAtAfter(
            Long driverId, AlertType alertType, LocalDateTime after);

    List<Alert> findByDriverIdAndAcknowledgedFalseOrderByCreatedAtDesc(Long driverId);

    // Bulk delete as a single SQL statement (see LocationPingCleanupJob's repo
    // method for why: a derived deleteBy... method would load every matching
    // row as an entity first). Only acknowledged alerts are eligible - anything
    // still unacknowledged is kept regardless of age, since it may still need
    // action.
    @Modifying
    @Query("delete from Alert a where a.acknowledged = true and a.createdAt < :cutoff")
    int deleteByAcknowledgedTrueAndCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
