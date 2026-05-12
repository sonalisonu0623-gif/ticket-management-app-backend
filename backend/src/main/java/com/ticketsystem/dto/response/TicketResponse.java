package com.ticketsystem.dto.response;

import com.ticketsystem.entity.Priority;
import com.ticketsystem.entity.SupportLevel;
import com.ticketsystem.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String ticketNumber;
    private String issueDescription;
    private Long projectId;
    private String projectName;
    private Long assignedToId;
    private String assignedToName;
    private Long createdById;
    private String createdByName;
    private SupportLevel supportLevel;
    private Priority priority;
    private LocalDateTime generationDateTime;
    private LocalDateTime responseDateTime;
    private LocalDateTime resolutionDateTime;
    private Long resolutionTimeMinutes;
    private String resolutionTimeFormatted;
    private TicketStatus currentStatus;
    private String resolutionDetails;
    private String remarks;
    private Boolean slaBreached;
    private LocalDateTime slaDueDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> comments;
    private List<HistoryResponse> history;
    private List<AttachmentResponse> attachments;
}
