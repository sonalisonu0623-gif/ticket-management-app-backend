package com.ticketsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ShiftDTO {

    private Long id;

    @NotBlank(message = "Shift name is required")
    private String shiftName;

    @NotBlank(message = "Start time is required")
    private String startTime;

    @NotBlank(message = "End time is required")
    private String endTime;

    private List<String> workingDays;

    private String timezone;
}