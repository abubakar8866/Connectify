package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnbanAppealRequest {

    @NotBlank(message = "Appeal message is required")
    @Size(
            min = 10,
            max = 1000,
            message = "Appeal message must be between 10 and 1000 characters"
    )
    private String message;

}

