package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.RejectReportRequest;
import com.abubakar.connectify.dto.request.ReportSearchRequest;
import com.abubakar.connectify.dto.request.ResolveReportRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.AdminReportResponse;

public interface AdminReportService {

    CursorPageResponse<AdminReportResponse>
    getReports(
            ReportSearchRequest request,
            Long cursor,
            int size
    );

    AdminReportResponse getReportDetails(
            Long reportId
    );

    void resolveReport(
            Long reportId,
            ResolveReportRequest request
    );

    void rejectReport(
            Long reportId,
            RejectReportRequest request
    );

    void deleteReport(
            Long reportId
    );

}

