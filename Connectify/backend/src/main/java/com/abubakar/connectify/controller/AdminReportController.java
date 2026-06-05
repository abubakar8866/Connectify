package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.RejectReportRequest;
import com.abubakar.connectify.dto.request.ReportSearchRequest;
import com.abubakar.connectify.dto.request.ResolveReportRequest;
import com.abubakar.connectify.dto.response.AdminReportResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.service.AdminReportService;
import com.abubakar.connectify.util.PaginationConstants;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminReportController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminReportController.class
            );

    @Autowired
    private AdminReportService adminReportService;

    @GetMapping
    public ResponseEntity<
            CursorPageResponse<AdminReportResponse>
            > getReports(

            ReportSearchRequest request,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                """
                API request received | get reports
                | status: {}
                | targetType: {}
                | reporterKeyword: {}
                | resolvedOnly: {}
                | cursor: {}
                | size: {}
                """,
                request.getStatus(),
                request.getTargetType(),
                request.getReporterKeyword(),
                request.getResolvedOnly(),
                cursor,
                size
        );

        return ResponseEntity.ok(
                adminReportService.getReports(
                        request,
                        cursor,
                        size
                )
        );
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<AdminReportResponse>
    getReportDetails(
            @PathVariable Long reportId
    ) {

        logger.info(
                "API request received | get report details | reportId: {}",
                reportId
        );

        return ResponseEntity.ok(
                adminReportService.getReportDetails(
                        reportId
                )
        );
    }

    @PutMapping("/{reportId}/resolve")
    public ResponseEntity<String>
    resolveReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ResolveReportRequest request
    ) {

        logger.info(
                "API request received | resolve report | reportId: {}",
                reportId
        );

        adminReportService.resolveReport(
                reportId,
                request
        );

        return ResponseEntity.ok(
                "Report resolved successfully"
        );
    }

    @PutMapping("/{reportId}/reject")
    public ResponseEntity<String>
    rejectReport(
            @PathVariable Long reportId,
            @Valid @RequestBody RejectReportRequest request
    ) {

        logger.info(
                "API request received | reject report | reportId: {}",
                reportId
        );

        adminReportService.rejectReport(
                reportId,
                request
        );

        return ResponseEntity.ok(
                "Report rejected successfully"
        );
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<String>
    deleteReport(
            @PathVariable Long reportId
    ) {

        logger.info(
                "API request received | delete report | reportId: {}",
                reportId
        );

        adminReportService.deleteReport(
                reportId
        );

        return ResponseEntity.ok(
                "Report deleted successfully"
        );
    }

}

