package com.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayDTO {
    private Long id;
    @NotBlank private String holidayName;
    @NotNull  private LocalDate holidayDate;
    private String description;
}
