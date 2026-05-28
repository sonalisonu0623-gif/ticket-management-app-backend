package com.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ShiftHoursDTO {
    private Long id;
    @NotBlank private String shiftName;
    @NotNull  private LocalTime startTime;
    @NotNull  private LocalTime endTime;
    private boolean isActive;
}
