package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank(message = "Project code is required")
    @Size(max = 20)
    private String projectCode;

    private Boolean isActive = true;
}
