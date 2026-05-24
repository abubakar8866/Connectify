package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.ReportReason;
import com.abubakar.connectify.enums.ReportStatus;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {

    private Long id;

    private String reportedByUsername;

    private Long postId;

    private Long commentId;

    private Long userId;

    private Long chatId;

    private Long messageId;

    private Long storyId;

    private ReportReason reason;

    private String description;

    private ReportStatus status;

    private LocalDateTime createdAt;

}

