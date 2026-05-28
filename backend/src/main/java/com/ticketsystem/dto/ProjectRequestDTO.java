package com.ticketsystem.dto;

import com.ticketsystem.entity.Project.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequestDTO {

    @NotBlank(message = "Project code is required")
    private String projectCode;

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String description;

    @NotNull(message = "Status is required")
    private ProjectStatus status;

    private LocalDate startDate;

    private LocalDate endDate;
}
