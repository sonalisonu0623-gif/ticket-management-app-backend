package com.ticketsystem.dto.response;

import com.ticketsystem.entity.Role;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String department;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
