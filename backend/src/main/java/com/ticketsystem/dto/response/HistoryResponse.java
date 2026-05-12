package com.ticketsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HistoryResponse {
    private Long id;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String description;
    private Long changedById;
    private String changedByName;
    private LocalDateTime changedAt;
}
