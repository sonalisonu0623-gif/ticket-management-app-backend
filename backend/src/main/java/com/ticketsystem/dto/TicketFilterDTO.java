package com.ticketsystem.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketFilterDTO {
    private String ticketNumber;
    private Long projectId;
    private Long employeeId;
    private String priority;
    private String currentStatus;
    private String supportLevel;
    private Boolean slaBreached;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}