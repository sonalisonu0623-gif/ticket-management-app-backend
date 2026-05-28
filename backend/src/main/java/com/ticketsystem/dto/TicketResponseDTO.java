package com.ticketsystem.dto;

import com.ticketsystem.entity.Ticket.CurrentStatus;
import com.ticketsystem.entity.Ticket.Priority;
import com.ticketsystem.entity.Ticket.SupportLevel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponseDTO {

    private Long id;
    private String ticketId;
    private String projectAssignment;
    private String issueDescription;
    private String assignedEmployee;
    private SupportLevel supportLevel;
    private Priority priority;
    private LocalDateTime generationDateTime;
    private LocalDateTime responseDateTime;
    private LocalDateTime resolutionTime;
    private CurrentStatus currentStatus;
    private String resolutionDetails;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── SLA enrichment fields (computed at query time) ───────────────────────
    /** Deadline by which the ticket must be resolved (working-hours aware) */
    private LocalDateTime slaDeadline;

    /** True if current time > slaDeadline and ticket is not yet resolved/closed */
    private boolean slaBreached;

    /** Working hours elapsed since ticket creation */
    private Long workingHoursElapsed;
}
