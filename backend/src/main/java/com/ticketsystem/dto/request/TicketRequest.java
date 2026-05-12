package com.ticketsystem.dto.request;

import com.ticketsystem.entity.Priority;
import com.ticketsystem.entity.SupportLevel;
import com.ticketsystem.entity.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Issue description is required")
    @Size(min = 10, max = 5000, message = "Issue description must be between 10 and 5000 characters")
    private String issueDescription;

    private Long assignedToId;

    @NotNull(message = "Support level is required")
    private SupportLevel supportLevel;

    @NotNull(message = "Priority is required")
    private Priority priority;

    private TicketStatus currentStatus;

    private String resolutionDetails;

    private String remarks;
}
