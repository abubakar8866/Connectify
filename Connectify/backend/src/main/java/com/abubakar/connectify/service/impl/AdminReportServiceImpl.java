package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminReportResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Report;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.ReportStatus;
import com.abubakar.connectify.enums.ReportTargetType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminReportService;
import com.abubakar.connectify.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminReportServiceImpl
        implements AdminReportService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminReportServiceImpl.class
            );

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Autowired
    private ReportAccessValidator reportAccessValidator;

    // ================= GET PENDING REPORTS =================
    @Override
    public CursorPageResponse<AdminReportResponse>
    getPendingReports(
            Long cursor,
            int size
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(
                admin
        );

        logger.info(
                """
                Fetching pending reports
                | adminId: {}
                | cursor: {}
                | size: {}
                """,
                admin.getId(),
                cursor,
                size
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Report> reports =
                cursor == null

                        ? reportRepository
                        .findByStatusOrderByIdDesc(
                                ReportStatus.PENDING,
                                pageable
                        )

                        : reportRepository
                        .findByStatusAndIdLessThanOrderByIdDesc(
                                ReportStatus.PENDING,
                                cursor,
                                pageable
                        );

        logger.info(
                """
                Pending reports fetched successfully
                | adminId: {}
                | count: {}
                """,
                admin.getId(),
                reports.size()
        );

        return CursorPaginationUtil.buildResponse(
                reports,
                size,
                Report::getId,
                this::mapToResponse
        );
    }

    // ================= GET REPORT DETAILS =================
    @Override
    public AdminReportResponse getReportDetails(
            Long reportId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(
                admin
        );

        logger.info(
                "Fetching report details | reportId: {}",
                reportId
        );

        Report report =
                reportAccessValidator
                        .getReport(reportId);

        logger.info(
                """
                Report details fetched successfully
                | reportId: {}
                | adminId: {}
                """,
                reportId,
                admin.getId()
        );

        return mapToResponse(report);

    }

    // ================= RESOLVE REPORT =================
    @Override
    public void resolveReport(
            Long reportId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(
                admin
        );

        logger.info(
                "Resolve report request | reportId: {}",
                reportId
        );

        Report report =
                reportAccessValidator
                        .getReport(reportId);

        validatePendingReport(report,admin);

        report.setStatus(
                ReportStatus.RESOLVED
        );

        reportRepository.save(
                report
        );

        logger.info(
                """
                Report resolved successfully
                | reportId: {}
                | adminId: {}
                """,
                reportId,
                admin.getId()
        );

    }

    // ================= REJECT REPORT =================
    @Override
    public void rejectReport(
            Long reportId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(
                admin
        );

        logger.info(
                "Reject report request | reportId: {}",
                reportId
        );

        Report report =
                reportAccessValidator
                        .getReport(reportId);

        validatePendingReport(report,admin);

        report.setStatus(
                ReportStatus.REJECTED
        );

        reportRepository.save(
                report
        );

        logger.info(
                """
                Report rejected successfully
                | reportId: {}
                | adminId: {}
                """,
                reportId,
                admin.getId()
        );

    }

    // ================= DELETE REPORT =================
    @Override
    public void deleteReport(
            Long reportId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(
                admin
        );

        logger.info(
                "Delete report request | reportId: {}",
                reportId
        );

        Report report =
                reportAccessValidator
                        .getReport(reportId);

        reportRepository.delete(
                report
        );

        logger.info(
                """
                Report deleted successfully
                | reportId: {}
                | adminId: {}
                """,
                reportId,
                admin.getId()
        );

    }

    // ================= PRIVATE METHODS =================
    private AdminReportResponse mapToResponse(
            Report report
    ) {

        return AdminReportResponse.builder()

                .id(report.getId())

                .reportedById(
                        report.getReportedBy().getId()
                )

                .reportedByUsername(
                        report.getReportedBy().getUname()
                )

                .reportedEntityType(
                        getReportedEntityType(report)
                )

                .reportedUserId(
                        report.getReportedUser() != null
                                ? report.getReportedUser().getId()
                                : null
                )

                .postId(
                        report.getPost() != null
                                ? report.getPost().getId()
                                : null
                )

                .commentId(
                        report.getComment() != null
                                ? report.getComment().getId()
                                : null
                )

                .chatId(
                        report.getChat() != null
                                ? report.getChat().getId()
                                : null
                )

                .messageId(
                        report.getMessage() != null
                                ? report.getMessage().getId()
                                : null
                )

                .storyId(
                        report.getStory() != null
                                ? report.getStory().getId()
                                : null
                )

                .reason(report.getReason())

                .description(
                        report.getDescription()
                )

                .status(
                        report.getStatus()
                )

                .createdAt(
                        report.getCreatedAt()
                )

                .build();
    }

    private ReportTargetType getReportedEntityType(
            Report report
    ) {

        if (report.getPost() != null) {
            return ReportTargetType.POST;
        }

        if (report.getComment() != null) {
            return ReportTargetType.COMMENT;
        }

        if (report.getReportedUser() != null) {
            return ReportTargetType.USER;
        }

        if (report.getStory() != null) {
            return ReportTargetType.STORY;
        }

        if (report.getChat() != null) {
            return ReportTargetType.CHAT;
        }

        return ReportTargetType.MESSAGE;
    }

    private void validatePendingReport(
            Report report,
            User admin
    ) {

        if (report.getStatus() == ReportStatus.PENDING) {
            return;
        }

        if (report.getStatus() == ReportStatus.RESOLVED) {

            logger.warn(
                    """
                    Resolve/Reject report failed
                    | reportId: {}
                    | adminId: {}
                    | currentStatus: {}
                    """,
                    report.getId(),
                    admin.getId(),
                    report.getStatus()
            );

            throw new OperationFailException(
                    "Report already resolved"
            );
        }

        if (report.getStatus() == ReportStatus.REJECTED) {

            logger.warn(
                    """
                    Resolve/Reject report failed
                    | reportId: {}
                    | adminId: {}
                    | currentStatus: {}
                    """,
                    report.getId(),
                    admin.getId(),
                    report.getStatus()
            );

            throw new OperationFailException(
                    "Report already rejected"
            );
        }

        logger.warn(
                """
                Invalid report status transition
                | reportId: {}
                | adminId: {}
                | status: {}
                """,
                report.getId(),
                admin.getId(),
                report.getStatus()
        );

        throw new OperationFailException(
                "Invalid report status"
        );
    }

}

