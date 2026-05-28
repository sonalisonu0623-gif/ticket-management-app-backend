package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", unique = true, nullable = false, length = 20)
    private String ticketId;

    @Column(name = "project_assignment", nullable = false, length = 100)
    private String projectAssignment;

    @Column(name = "issue_description", columnDefinition = "TEXT", nullable = false)
    private String issueDescription;

    @Column(name = "assigned_employee", length = 100)
    private String assignedEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_level", nullable = false, length = 10)
    private SupportLevel supportLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    @Column(name = "generation_date_time")
    private LocalDateTime generationDateTime;

    @Column(name = "response_date_time")
    private LocalDateTime responseDateTime;

    @Column(name = "resolution_time")
    private LocalDateTime resolutionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private CurrentStatus currentStatus;

    @Column(name = "resolution_details", columnDefinition = "TEXT")
    private String resolutionDetails;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SupportLevel {
        L1, L2, L3
    }

    public enum Priority {
        P1_CRITICAL, P2_HIGH, P3_MEDIUM, P4_LOW
    }

    public enum CurrentStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
}
