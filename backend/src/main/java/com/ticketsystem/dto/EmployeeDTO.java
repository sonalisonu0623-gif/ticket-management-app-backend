package com.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "Employee name is required")
    private String employeeName;

    private String supportLevel;
}
