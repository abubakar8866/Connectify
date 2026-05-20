package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateChatRequest {

    @NotNull(message = "Receiver id is required")
    private Long receiverId;

}

