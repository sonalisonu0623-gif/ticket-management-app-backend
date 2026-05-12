package com.ticketsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String content;
    private Boolean isInternal;
    private LocalDateTime createdAt;
}
