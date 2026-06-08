package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatResponse {

    private Long chatId;

    private Long otherUserId;

    private Boolean deletedByAdmin;

    private Boolean restoreRequested;

    private String username;

    private String profileImageUrl;

    private Boolean isVerified;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private Boolean isOnline;

    private LocalDateTime lastSeenAt;

    private Long unreadCount;

}

