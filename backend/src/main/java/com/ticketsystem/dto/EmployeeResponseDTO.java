package com.ticketsystem.dto;

import com.ticketsystem.entity.Employee.EmployeeStatus;
import com.ticketsystem.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EmployeeResponseDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String employeeName;
    private String designation;
    private String department;
    private EmployeeStatus status;
    private boolean isActive;

    // Shift summary
    private Long shiftId;
    private String shiftName;

    // Projects
    private List<ProjectSummaryDTO> projects;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data @Builder
    public static class ProjectSummaryDTO {
        private Long id;
        private String projectCode;
        private String projectName;
    }
}
