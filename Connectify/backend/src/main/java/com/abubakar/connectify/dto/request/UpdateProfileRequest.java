package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(min = 3, max = 20,
            message = "Username must be between 3 and 20 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9._]+$",
            message = "Username can contain only letters, numbers, dot and underscore"
    )
    private String uname;

    @Size(max = 500,
            message = "Bio must not exceed 500 characters")
    private String bio;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50,
            message = "Name must be between 2 and 50 characters")
    private String name;

    private Gender gender;

    private List<
            @Size(min = 2, max = 30,
                    message = "Language name must be between 2 and 30 characters")
                    String> languages;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 100,
            message = "City name must not exceed 100 characters")
    private String city;

}

