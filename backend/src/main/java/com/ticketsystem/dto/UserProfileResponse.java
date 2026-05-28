package com.ticketsystem.dto;

import com.ticketsystem.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private Long employeeId;
    private boolean isActive;
    private LocalDateTime createdAt;
}
