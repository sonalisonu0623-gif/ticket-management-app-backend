package com.ticketsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SlaConfigDTO {

    private Long id;

    @NotNull(message = "Project is required")
    private Long projectId;
    private String projectName;

    @NotBlank(message = "Priority level is required")
    private String priorityLevel;

    @Min(1) private Integer responseTimeSla;
    @Min(1) private Integer resolutionTimeSla;
    @Min(1) private Integer escalationTimeSla;
}