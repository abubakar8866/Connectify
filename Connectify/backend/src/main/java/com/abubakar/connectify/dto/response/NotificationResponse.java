package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;

    private NotificationType type;

    private String message;

    private Boolean isRead;

    private Long senderId;

    private String senderUsername;

    private String senderProfileImage;

    private Long postId;

    private Long commentId;

    private LocalDateTime createdAt;

}

