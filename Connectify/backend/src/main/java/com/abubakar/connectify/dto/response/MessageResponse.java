package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {

    private Long id;

    private String content;

    private String mediaUrl;

    private MessageType messageType;

    private Boolean isSeen;

    private LocalDateTime seenAt;

    private Boolean isEdited;

    private LocalDateTime editedAt;

    private Boolean isDeletedForMe;

    private Boolean deletedForEveryone;

    private Long senderId;

    private String senderUsername;

    private String senderProfileImage;

    private Long replyMessageId;

    private String replyMessageContent;

    private String replySenderUsername;

    private MessageType replyMessageType;

    private String replyMediaUrl;

    private LocalDateTime createdAt;

}

