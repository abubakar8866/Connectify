package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {

    @NotBlank(message = "Caption is required")
    @Size(
            min = 2,
            max = 2000,
            message = "Caption must be between 2 and 2000 characters"
    )
    private String caption;

}

