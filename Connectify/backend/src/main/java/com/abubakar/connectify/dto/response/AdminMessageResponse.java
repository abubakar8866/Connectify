package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminMessageResponse {

    private Long messageId;

    private Long chatId;

    private Long senderId;

    private String senderUsername;

    private String content;

    private String mediaUrl;

    private MessageType messageType;

    private Boolean isSeen;

    private Boolean isEdited;

    private Boolean deletedForEveryone;

    private Boolean deletedByAdmin;

    private Boolean restoreRequested;

    private Long reportCount;

    private LocalDateTime createdAt;

}

