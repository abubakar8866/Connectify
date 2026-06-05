package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.AdminAction;
import com.abubakar.connectify.enums.ReportReason;
import com.abubakar.connectify.enums.ReportStatus;

import com.abubakar.connectify.enums.ReportTargetType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminReportResponse {

    private Long id;

    private Long reportedById;

    private String reportedByUsername;

    private ReportTargetType reportedEntityType;

    private AdminAction adminAction;

    private String adminNote;

    private Long resolvedById;

    private String resolvedByUsername;

    private LocalDateTime resolvedAt;

    private Long reportedUserId;

    private Long postId;

    private Long commentId;

    private Long chatId;

    private Long messageId;

    private Long storyId;

    private ReportReason reason;

    private String description;

    private ReportStatus status;

    private LocalDateTime createdAt;

}

