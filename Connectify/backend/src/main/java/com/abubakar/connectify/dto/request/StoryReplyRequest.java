package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoryReplyRequest {

    @NotBlank(message = "Reply message required")
    @Size(max = 500, message = "Reply message size exceeded to 500.")
    private String message;

}

