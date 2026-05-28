package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * SLA target response / resolution times (in hours) per priority + support level.
 * Calculations are performed against shift hours, excluding weekends and holidays.
 */
@Entity
@Table(name = "sla_config",
       uniqueConstraints = @UniqueConstraint(columnNames = {"priority", "support_level"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SlaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority;           // P1_CRITICAL, P2_HIGH, P3_MEDIUM, P4_LOW

    @Column(name = "support_level", nullable = false, length = 10)
    private String supportLevel;       // L1, L2, L3

    /** Target time to first response in working hours */
    @Column(name = "response_time_hours", nullable = false)
    private double responseTimeHours;

    /** Target time to full resolution in working hours */
    @Column(name = "resolution_time_hours", nullable = false)
    private double resolutionTimeHours;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
