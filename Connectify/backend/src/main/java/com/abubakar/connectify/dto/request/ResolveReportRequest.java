package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.AdminAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResolveReportRequest {

    @NotNull(message = "Admin action is required")
    private AdminAction adminAction;

    @Size(
            min = 2,
            max = 1000,
            message = "Note character should have 2 to 1000 characters long."
    )
    private String adminNote;

}

