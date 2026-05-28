package com.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SlaConfigDTO {
    private Long id;
    @NotBlank private String priority;
    @NotBlank private String supportLevel;
    @Positive private double responseTimeHours;
    @Positive private double resolutionTimeHours;
    private boolean isActive;
}
