package com.ticketsystem.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardDTO {

    private Long projectId;
    private String projectName;

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long pendingTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long escalatedTickets;
    private long criticalTickets;
    private long slaBreachedTickets;

    private Double avgResolutionHours;
    private Double slaComplianceRate;

    /** key = status name, value = count */
    private Map<String, Long> ticketsByStatus;

    /** key = priority name, value = count */
    private Map<String, Long> ticketsByPriority;

    /** key = supportLevel (L1/L2/L3), value = count */
    private Map<String, Long> ticketsBySupportLevel;

    private List<EmployeePerformanceDTO> employeePerformance;
    private List<TicketDTO> recentTickets;
    private List<TicketDTO> criticalOpenTickets;
}
