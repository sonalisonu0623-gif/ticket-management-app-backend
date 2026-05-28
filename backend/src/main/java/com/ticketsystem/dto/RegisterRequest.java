package com.ticketsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    /**
     * Accepted values: ADMIN, PROJECT_MANAGER, L1_SUPPORT, L2_SUPPORT, L3_SUPPORT, USER
     * Defaults to USER if absent or unrecognised.
     */
    private String role;
}
