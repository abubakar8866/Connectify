package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.ReportReason;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportRequest {

    @NotNull(message = "Reason is required")
    private ReportReason reason;

    private String description;

}

