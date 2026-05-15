package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.CreateReportRequest;
import com.abubakar.connectify.dto.response.ReportResponse;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.enums.ReportStatus;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.*;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.service.ReportService;

import com.abubakar.connectify.util.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NotificationService notificationService;

    @Override
    public ReportResponse reportPost(
            Long postId,
            CreateReportRequest request
    ) {

        logger.info(
                "Reporting post | postId: {}",
                postId
        );

        User currentUser = this.authUtil.getCurrentUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "Post not found"
                        )
                );

        boolean alreadyReported =
                reportRepository
                        .findByReportedByAndPost(
                                currentUser,
                                post
                        )
                        .isPresent();

        if (alreadyReported) {

            throw new OperationFailException(
                    "You already reported this post"
            );
        }

        Report report = Report.builder()
                .reportedBy(currentUser)
                .post(post)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);

        // NOTIFY POST OWNER
        notificationService.createNotification(
                post.getUser().getId(),                 // receiver
                currentUser.getId(),                   // sender
                currentUser.getUname() +
                        " reported your post",
                NotificationType.REPORT,
                post.getId(),
                null
        );

        // NOTIFY ADMIN
        userRepository.findByRole(
                com.abubakar.connectify.enums.Role.ADMIN
        ).ifPresent(admin ->

                notificationService.createNotification(
                        admin.getId(),
                        currentUser.getId(),
                        currentUser.getUname() +
                                " reported a post",
                        NotificationType.REPORT,
                        post.getId(),
                        null
                )
        );

        logger.info(
                "Post reported successfully"
        );

        return mapToResponse(report);
    }

    @Override
    public ReportResponse reportComment(
            Long commentId,
            CreateReportRequest request
    ) {

        logger.info(
                "Reporting comment | commentId: {}",
                commentId
        );

        User currentUser = this.authUtil.getCurrentUser();

        Comment comment =
                commentRepository.findById(commentId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Comment not found"
                                )
                        );

        boolean alreadyReported =
                reportRepository
                        .findByReportedByAndComment(
                                currentUser,
                                comment
                        )
                        .isPresent();

        if (alreadyReported) {

            throw new OperationFailException(
                    "You already reported this comment"
            );
        }

        Report report = Report.builder()
                .reportedBy(currentUser)
                .comment(comment)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);

        // NOTIFY COMMENT OWNER
        notificationService.createNotification(
                comment.getUser().getId(),
                currentUser.getId(),
                currentUser.getUname() +
                        " reported your comment",
                NotificationType.REPORT,
                comment.getPost().getId(),
                comment.getId()
        );

        // NOTIFY ADMIN
        userRepository.findByRole(
                com.abubakar.connectify.enums.Role.ADMIN
        ).ifPresent(admin ->

                notificationService.createNotification(
                        admin.getId(),
                        currentUser.getId(),
                        currentUser.getUname() +
                                " reported a comment",
                        NotificationType.REPORT,
                        comment.getPost().getId(),
                        comment.getId()
                )
        );

        logger.info(
                "Comment reported successfully"
        );

        return mapToResponse(report);
    }

    @Override
    public ReportResponse reportUser(
            Long userId,
            CreateReportRequest request
    ) {

        logger.info(
                "Reporting user | userId: {}",
                userId
        );

        User currentUser = this.authUtil.getCurrentUser();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "User not found"
                        )
                );

        boolean alreadyReported =
                reportRepository
                        .findByReportedByAndReportedUser(
                                currentUser,
                                user
                        )
                        .isPresent();

        if (alreadyReported) {

            throw new OperationFailException(
                    "You already reported this user"
            );
        }

        Report report = Report.builder()
                .reportedBy(currentUser)
                .reportedUser(user)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);

        // NOTIFY REPORTED USER
        notificationService.createNotification(
                user.getId(),
                currentUser.getId(),
                currentUser.getUname() +
                        " reported your profile",
                NotificationType.REPORT,
                null,
                null
        );

        // NOTIFY ADMIN
        userRepository.findByRole(
                com.abubakar.connectify.enums.Role.ADMIN
        ).ifPresent(admin ->

                notificationService.createNotification(
                        admin.getId(),
                        currentUser.getId(),
                        currentUser.getUname() +
                                " reported a user",
                        NotificationType.REPORT,
                        null,
                        null
                )
        );

        logger.info(
                "User reported successfully"
        );

        return mapToResponse(report);
    }

    // ================= HELPERS =================
    private ReportResponse mapToResponse(
            Report report
    ) {

        return ReportResponse.builder()

                .id(report.getId())

                .reportedByUsername(
                        report.getReportedBy()
                                .getUname()
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

                .userId(
                        report.getReportedUser() != null
                                ? report.getReportedUser().getId()
                                : null
                )

                .reason(report.getReason())

                .description(
                        report.getDescription()
                )

                .status(report.getStatus())

                .createdAt(
                        report.getCreatedAt()
                )

                .build();
    }

}

