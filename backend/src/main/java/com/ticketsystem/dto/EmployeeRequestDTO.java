package com.ticketsystem.dto;

import com.ticketsystem.entity.Employee.EmployeeStatus;
import com.ticketsystem.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class EmployeeRequestDTO {
    @NotBlank private String username;
    @NotBlank @Email private String email;
    private String password;              // optional on update

    @NotNull  private Role role;
    @NotBlank private String employeeName;
    private String designation;
    private String department;
    @NotNull  private EmployeeStatus status;

    private Long shiftId;                 // optional shift assignment (null = no specific shift)
    private Set<Long> projectIds;         // many-to-many project assignment
}
