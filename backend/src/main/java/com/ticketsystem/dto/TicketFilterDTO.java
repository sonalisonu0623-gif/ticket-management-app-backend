package com.ticketsystem.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketFilterDTO {
    private String ticketNumber;
    private Long projectId;
    private Long employeeId;
    private String priority;
    private String currentStatus;
    private String supportLevel;
}
