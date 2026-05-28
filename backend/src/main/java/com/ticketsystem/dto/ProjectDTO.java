package com.ticketsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectDTO {

    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 150, message = "Project name must be 2–150 characters")
    private String projectName;

    @Size(max = 30, message = "Project code max 30 characters")
    private String projectCode;

    private String description;

    @Email(message = "Invalid support email")
    private String supportEmail;

    @Min(value = 1, message = "SLA hours must be at least 1")
    private Integer slaHours;

    private String shiftTiming;

    /** ACTIVE | INACTIVE */
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Summary of assigned employees (returned on GET, ignored on POST/PUT) */
    private List<EmployeeDTO> employees;
}
