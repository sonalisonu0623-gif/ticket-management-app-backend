package com.ticketsystem.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeePerformanceDTO {
    private Long employeeId;
    private String employeeName;
    private String supportLevel;
    private long totalAssigned;
    private long resolved;
    private long open;
    private long inProgress;
    private long slaBreached;
    private Double avgResolutionHours;
}
