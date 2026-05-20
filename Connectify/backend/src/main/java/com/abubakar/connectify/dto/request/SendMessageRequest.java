package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.MessageType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class SendMessageRequest {

    @Size(
            max = 5000,
            message = "Message cannot exceed 5000 characters"
    )
    private String content;

    @NotNull(message = "Message type required")
    private MessageType messageType;

    private Long replyToMessageId;

}

