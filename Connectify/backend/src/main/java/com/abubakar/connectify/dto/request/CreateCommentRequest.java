package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Comment content required.")
    private String content;

    private Long parentCommentId;
}

