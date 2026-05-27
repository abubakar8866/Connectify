package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Comment content required.")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String content;

    private Long parentCommentId;

}

