package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.RejectReportRequest;
import com.abubakar.connectify.dto.request.ReportSearchRequest;
import com.abubakar.connectify.dto.request.ResolveReportRequest;
import com.abubakar.connectify.dto.response.AdminReportResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.*;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.*;
import com.abubakar.connectify.service.AdminReportService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.specification.ReportSpecification;
import com.abubakar.connectify.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Autowired
    private ReportAccessValidator reportAccessValidator;

    // ================= GET REPORTS =================
    @Override
    public CursorPageResponse<AdminReportResponse>
    getReports(
            ReportSearchRequest request,
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
                Fetching reports
                | adminId: {}
                | status: {}
                | targetType: {}
                | reporterKeyword: {}
                | resolvedOnly: {}
                | cursor: {}
                | size: {}
                """,
                admin.getId(),
                request.getStatus(),
                request.getTargetType(),
                request.getReporterKeyword(),
                request.getResolvedOnly(),
                cursor,
                size
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        Specification<Report> specification =
                ReportSpecification.searchReports(
                        request.getStatus(),
                        request.getTargetType(),
                        request.getReporterKeyword(),
                        request.getResolvedOnly(),
                        cursor
                );

        List<Report> reports =
                reportRepository.findAll(
                        specification,
                        pageable
                ).getContent();

        logger.info(
                """
                Reports fetched successfully
                | adminId: {}
                | status: {}
                | targetType: {}
                | fetchedCount: {}
                | nextCursor: {}
                """,
                admin.getId(),
                request.getStatus(),
                request.getTargetType(),
                reports.size(),
                reports.isEmpty()
                        ? null
                        : reports.getLast().getId()
        );

        return CursorPaginationUtil.buildResponse(
                reports,
                size,
                Report::getId,
                this::mapToResponse
        );
    }

    // ================= GET Single REPORT =================
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
            Long reportId,
            ResolveReportRequest request
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

        report.setAdminAction(
                request.getAdminAction()
        );

        report.setAdminNote(
                request.getAdminNote()
        );

        report.setResolvedBy(
                admin
        );

        report.setResolvedAt(
                LocalDateTime.now()
        );

        applyAdminAction(
                report,
                request.getAdminAction()
        );

        reportRepository.save(
                report
        );

        notificationService.createNotification(
                report.getReportedBy().getId(),
                admin.getId(),
                "Your report has been resolved",
                NotificationType.REPORT_RESOLVED,
                null,
                null
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
            Long reportId,
            RejectReportRequest request
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

        report.setAdminNote(
                request.getAdminNote()
        );

        report.setResolvedBy(
                admin
        );

        report.setResolvedAt(
                LocalDateTime.now()
        );

        reportRepository.save(
                report
        );

        notificationService.createNotification(
                report.getReportedBy().getId(),
                admin.getId(),
                "Your report has been rejected",
                NotificationType.REPORT_REJECTED,
                null,
                null
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

    // ================= PRIVATE METHODS =================

    private void applyAdminAction(
            Report report,
            AdminAction action
    ) {

        switch (action) {

            case NO_VIOLATION:
            case WARNING:
                return;

            case HIDE_CONTENT:
                hideTarget(report);
                break;

            case BAN_USER:
                banTargetUser(report);
                break;
        }
    }

    private void hideTarget(
            Report report
    ) {

        if (report.getPost() != null) {
            report.getPost().setDeleted(true);
            postRepository.save(report.getPost());
        }

        if (report.getComment() != null) {
            report.getComment().setDeleted(true);
            commentRepository.save(report.getComment());
        }

        if (report.getStory() != null) {
            report.getStory().setDeleted(true);
            report.getStory().setDeletedByAdmin(true);
            report.getStory().setDeletedByAdminAt(LocalDateTime.now());
            storyRepository.save(report.getStory());
        }

        if(report.getMessage() != null){
            report.getMessage().setDeletedByAdmin(true);
            report.getMessage().setDeletedByAdminAt(LocalDateTime.now());
            messageRepository.save(report.getMessage());
        }

        if(report.getChat() != null){
            report.getChat().setDeletedByAdmin(true);
            report.getChat().setDeletedByAdminAt(LocalDateTime.now());
            chatRepository.save(report.getChat());
        }
    }

    private void banTargetUser(
            Report report
    ) {

        User target = report.getReportedUser();

        if (target == null) {
            return;
        }

        target.setAccountStatus(
                AccountStatus.BANNED
        );
    }

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

                .reportedName(
                        report.getReportedUser() != null
                            ? report.getReportedUser().getName()
                                : null
                )

                .reportedUserName(
                        report.getReportedUser() != null
                                ? report.getReportedUser().getUname()
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

                .reason(
                        report.getReason()
                )

                .description(
                        report.getDescription()
                )

                .status(
                        report.getStatus()
                )

                .adminAction(
                        report.getAdminAction()
                )

                .adminNote(
                        report.getAdminNote()
                )

                .resolvedById(
                        report.getResolvedBy() != null
                                ? report.getResolvedBy().getId()
                                : null
                )

                .resolvedByUsername(
                        report.getResolvedBy() != null
                                ? report.getResolvedBy().getUname()
                                : null
                )

                .resolvedAt(
                        report.getResolvedAt()
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

