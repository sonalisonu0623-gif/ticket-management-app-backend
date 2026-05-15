package com.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {
    private Long id;
    private String ticketNumber;

    @NotNull(message = "Project is required")
    private Long projectId;
    private String projectName;

    @NotBlank(message = "Issue description is required")
    private String issueDescription;

    private Long assignedEmployeeId;
    private String assignedEmployeeName;

    private String supportLevel;
    private String priority;

    private LocalDateTime generationDatetime;
    private LocalDateTime responseDatetime;
    private String resolutionTime;

    private String currentStatus;
    private String resolutionDetails;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
