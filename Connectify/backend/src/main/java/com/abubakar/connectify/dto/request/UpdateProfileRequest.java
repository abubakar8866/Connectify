package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(min = 3, max = 20,
            message = "Username must be between 3 and 20 characters")
    private String uname;

    @Size(max = 500,
            message = "Bio must not exceed 500 characters")
    private String bio;

    private String name;

}
