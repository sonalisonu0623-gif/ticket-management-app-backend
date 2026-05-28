package com.ticketsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectAssignmentRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Project IDs are required")
    private List<Long> projectIds;

    /** Optional role override for this project assignment */
    private String roleInProject;
}
