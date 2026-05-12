package com.ticketsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_number", columnList = "ticket_number"),
    @Index(name = "idx_ticket_status", columnList = "current_status"),
    @Index(name = "idx_ticket_priority", columnList = "priority"),
    @Index(name = "idx_ticket_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 20)
    private String ticketNumber;

    @Column(name = "issue_description", nullable = false, columnDefinition = "TEXT")
    private String issueDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_level", nullable = false)
    private SupportLevel supportLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(name = "generation_date_time", nullable = false)
    private LocalDateTime generationDateTime;

    @Column(name = "response_date_time")
    private LocalDateTime responseDateTime;

    @Column(name = "resolution_date_time")
    private LocalDateTime resolutionDateTime;

    @Column(name = "resolution_time_minutes")
    private Long resolutionTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    @Builder.Default
    private TicketStatus currentStatus = TicketStatus.OPEN;

    @Column(name = "resolution_details", columnDefinition = "TEXT")
    private String resolutionDetails;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "sla_breached")
    @Builder.Default
    private Boolean slaBreached = false;

    @Column(name = "sla_due_date_time")
    private LocalDateTime slaDueDateTime;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<TicketComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<TicketHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();
}
