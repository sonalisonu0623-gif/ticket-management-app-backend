package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_project",  columnList = "project_id"),
    @Index(name = "idx_ticket_employee", columnList = "assigned_employee_id"),
    @Index(name = "idx_ticket_status",   columnList = "current_status"),
    @Index(name = "idx_ticket_priority", columnList = "priority")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", unique = true, nullable = false, length = 30)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "issue_description", nullable = false, columnDefinition = "TEXT")
    private String issueDescription;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_employee_id")
    private Employee assignedEmployee;

    /** L1 | L2 | L3 */
    @Column(name = "support_level", length = 10)
    private String supportLevel;

    /** P1 - Critical | P2 - High | P3 - Medium | P4 - Low */
    @Column(name = "priority", length = 30)
    private String priority;

    @Column(name = "generation_datetime")
    private LocalDateTime generationDatetime;

    @Column(name = "response_datetime")
    private LocalDateTime responseDatetime;

    /** Human-readable resolution time e.g. "3h 20m" */
    @Column(name = "resolution_time", length = 50)
    private String resolutionTime;

    /** Business hours actually elapsed (numeric, for SLA calculations) */
    @Column(name = "business_hours_elapsed")
    private Integer businessHoursElapsed;

    /** Open | In Progress | Pending | Resolved | Closed | Escalated */
    @Column(name = "current_status", length = 30)
    @Builder.Default
    private String currentStatus = "Open";

    @Column(name = "resolution_details", columnDefinition = "TEXT")
    private String resolutionDetails;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /** True when SLA has been breached at time of last update */
    @Column(name = "sla_breached")
    @Builder.Default
    private Boolean slaBreached = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
