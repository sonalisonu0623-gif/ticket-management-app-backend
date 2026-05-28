package com.ticketsystem.dto;

import com.ticketsystem.entity.Ticket.CurrentStatus;
import com.ticketsystem.entity.Ticket.Priority;
import com.ticketsystem.entity.Ticket.SupportLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequestDTO {

    @NotBlank(message = "Project assignment is required")
    private String projectAssignment;

    @NotBlank(message = "Issue description is required")
    private String issueDescription;

    private String assignedEmployee;

    @NotNull(message = "Support level is required")
    private SupportLevel supportLevel;

    @NotNull(message = "Priority is required")
    private Priority priority;

    private LocalDateTime generationDateTime;

    private LocalDateTime responseDateTime;

    private LocalDateTime resolutionTime;

    @NotNull(message = "Current status is required")
    private CurrentStatus currentStatus;

    private String resolutionDetails;

    private String remarks;
}
