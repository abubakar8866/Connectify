package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BanUserRequest {

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500,
            message = "Reason must be between 5 and 500 characters")
    private String reason;

    @Min(value = 1,
            message = "Ban duration must be at least 1 day")
    @Max(value = 3650,
            message = "Ban duration cannot exceed 3650 days")
    private Integer durationInDays;

    @Size(max = 1000,
            message = "Admin note must not exceed 1000 characters")
    private String adminNote;

    private Boolean permanent;

}

