package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EditMessageRequest {

    @NotBlank(message = "Message content required")
    @Size(
            min = 1,
            max = 5000,
            message = "Message must be between 1 and 5000 characters"
    )
    private String content;

}

