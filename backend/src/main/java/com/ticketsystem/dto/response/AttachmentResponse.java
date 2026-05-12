package com.ticketsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String downloadUrl;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}
