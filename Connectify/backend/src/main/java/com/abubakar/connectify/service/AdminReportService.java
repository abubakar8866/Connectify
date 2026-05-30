package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.AdminReportResponse;

public interface AdminReportService {

    CursorPageResponse<AdminReportResponse>
    getPendingReports(
            Long cursor,
            int size
    );

    AdminReportResponse getReportDetails(
            Long reportId
    );

    void resolveReport(
            Long reportId
    );

    void rejectReport(
            Long reportId
    );

    void deleteReport(
            Long reportId
    );

}

