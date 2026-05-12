package com.ticketsystem.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String projectCode;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long ticketCount;
}
