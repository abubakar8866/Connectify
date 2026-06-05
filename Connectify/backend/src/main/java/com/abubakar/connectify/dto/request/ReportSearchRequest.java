package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.ReportStatus;
import com.abubakar.connectify.enums.ReportTargetType;

import lombok.Data;

@Data
public class ReportSearchRequest {

    private ReportStatus status;

    private ReportTargetType targetType;

    private String reporterKeyword;

    private Boolean resolvedOnly;

}

