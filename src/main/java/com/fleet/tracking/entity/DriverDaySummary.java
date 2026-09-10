package com.fleet.tracking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "driver_day_summaries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"driver_id", "summaryDate"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDaySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(nullable = false)
    private LocalDate summaryDate;

    @Column(nullable = false)
    private long totalDriveMinutes;

    @Column(nullable = false)
    private long thresholdMinutes;

    @Column(nullable = false)
    private boolean thresholdMet;

    // Minutes still owed from previous day(s), carried forward until acknowledged/made up
    @Column(nullable = false)
    @Builder.Default
    private long carryOverShortfallMinutes = 0;
}
