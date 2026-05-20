package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminChatResponse {

    private Long chatId;

    private String firstUsername;

    private String secondUsername;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private Long totalMessages;

    private Boolean deletedByAdmin;

    private LocalDateTime createdAt;

}

