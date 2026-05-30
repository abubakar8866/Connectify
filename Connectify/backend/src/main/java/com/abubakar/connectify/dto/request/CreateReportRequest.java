package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.ReportReason;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReportRequest {

    @NotNull(message = "Reason is required")
    private ReportReason reason;

    @Size(
            max = 1000,
            message = "Description cannot exceed 1000 characters"
    )
    private String description;

}

