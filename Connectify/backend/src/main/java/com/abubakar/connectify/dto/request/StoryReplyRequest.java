package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoryReplyRequest {

    @NotBlank(message = "Reply message required")
    private String message;

}
