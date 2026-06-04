package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MessageResponse {

    private Long id;

    private String content;

    private List<MessageMediaResponse> mediaFiles;

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

    private List<MessageMediaResponse> replyMediaFiles;

    private LocalDateTime createdAt;

}

