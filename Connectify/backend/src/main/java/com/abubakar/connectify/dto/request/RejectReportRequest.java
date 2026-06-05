package com.abubakar.connectify.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectReportRequest {

    @Size(
            min = 2,
            max = 1000,
            message = "Note character should have 2 to 1000 characters long."
    )
    private String adminNote;

}

