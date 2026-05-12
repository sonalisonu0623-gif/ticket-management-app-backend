package com.ticketsystem.dto.response;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardResponse {
    private Long totalTickets;
    private Long openTickets;
    private Long inProgressTickets;
    private Long resolvedTickets;
    private Long closedTickets;
    private Long criticalTickets;
    private Long slaBreachedTickets;
    private Double avgResolutionTimeMinutes;

    private Map<String, Long> statusDistribution;
    private Map<String, Long> priorityDistribution;
    private List<MonthlyTrendDto> monthlyTrends;
    private List<EmployeeWorkloadDto> employeeWorkload;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyTrendDto {
        private String month;
        private Long count;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EmployeeWorkloadDto {
        private String employeeName;
        private Long ticketCount;
    }
}
