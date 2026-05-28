package com.ticketsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeDTO {

    private Long id;

    private String employeeId;

    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 150, message = "Name must be 2–150 characters")
    private String employeeName;

    @Email(message = "Invalid email")
    private String email;

    /** L1 | L2 | L3 */
    private String supportLevel;

    /** ADMIN | PROJECT_MANAGER | L1_SUPPORT | L2_SUPPORT | L3_SUPPORT */
    private String role;

    private String designation;
    private String shift;

    /** ACTIVE | INACTIVE */
    private String status;

    /** IDs of projects this employee belongs to */
    private List<Long> projectIds;

    /** Names of projects (for display on GET) */
    private List<String> projectNames;

    private LocalDateTime createdAt;
}
