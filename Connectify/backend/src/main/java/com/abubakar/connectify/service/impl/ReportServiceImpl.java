package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.CreateReportRequest;
import com.abubakar.connectify.dto.response.ReportResponse;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.enums.ReportStatus;
import com.abubakar.connectify.enums.Role;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.*;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.service.ReportService;

import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.ValidateUserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
    private ChatRepository chatRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ValidateUserAccess validateUserAccess;

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

        // DELETED POST VALIDATION
        if (Boolean.TRUE.equals(post.getDeleted())) {

            throw new OperationFailException(
                    "Deleted post cannot be reported"
            );
        }

        // BANNED OWNER VALIDATION
        if (post.getUser().getAccountStatus()
                == AccountStatus.BANNED) {

            throw new OperationFailException(
                    "This post is unavailable"
            );
        }

        // SELF REPORT VALIDATION
        if (post.getUser().getId()
                .equals(currentUser.getId())) {

            throw new OperationFailException(
                    "You cannot report your own post"
            );
        }


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
                Role.ADMIN
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

        // DELETED COMMENT VALIDATION
        if (Boolean.TRUE.equals(comment.getDeleted())) {

            throw new OperationFailException(
                    "Deleted comment cannot be reported"
            );
        }

        // BANNED OWNER VALIDATION
        if (comment.getUser().getAccountStatus()
                == AccountStatus.BANNED) {

            throw new OperationFailException(
                    "This comment is unavailable"
            );
        }

        // SELF REPORT VALIDATION
        if (comment.getUser().getId()
                .equals(currentUser.getId())) {

            throw new OperationFailException(
                    "You cannot report your own comment"
            );
        }

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
                Role.ADMIN
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

        User user = this.validateUserAccess.getValidUser(userId);

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
                Role.ADMIN
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

    @Override
    public ReportResponse reportChat(
            Long chatId,
            CreateReportRequest request
    ) {

        User currentUser =
                authUtil.getCurrentUser();

        Chat chat =
                chatRepository.findById(chatId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Chat not found"
                                )
                        );

        if (Boolean.TRUE.equals(chat.getDeletedByAdmin())) {

            throw new OperationFailException(
                    "Chat already removed"
            );
        }

        boolean participant =
                chatParticipantRepository
                        .existsByChatAndUser(
                                chat,
                                currentUser
                        );

        if (!participant) {

            throw new OperationFailException(
                    "You are not part of this chat"
            );
        }

        boolean alreadyReported =
                reportRepository
                        .findByReportedByAndChat(
                                currentUser,
                                chat
                        )
                        .isPresent();

        if (alreadyReported) {

            throw new OperationFailException(
                    "You already reported this chat"
            );
        }

        Report report =
                Report.builder()
                        .reportedBy(currentUser)
                        .chat(chat)
                        .reason(request.getReason())
                        .description(request.getDescription())
                        .status(ReportStatus.PENDING)
                        .build();

        reportRepository.save(report);

        return mapToResponse(report);
    }

    @Override
    public ReportResponse reportMessage(
            Long messageId,
            CreateReportRequest request
    ) {

        logger.info(
                "Reporting message | messageId: {}",
                messageId
        );

        User currentUser =
                authUtil.getCurrentUser();

        Message message =
                messageRepository.findById(messageId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Message not found"
                                )
                        );

        // CHAT VALIDATION
        Chat chat = message.getChat();

        if (chat == null) {

            throw new OperationFailException(
                    "Chat not found"
            );
        }

        // CHAT DELETED VALIDATION
        if (Boolean.TRUE.equals(chat.getDeletedByAdmin())) {

            throw new OperationFailException(
                    "Chat is unavailable"
            );
        }

        // USER MUST BELONG TO CHAT
        boolean participantExists =
                chatParticipantRepository
                        .existsByChatAndUser(
                                chat,
                                currentUser
                        );

        if (!participantExists) {

            throw new OperationFailException(
                    "You are not part of this chat"
            );
        }

        // MESSAGE DELETED VALIDATION
        if (Boolean.TRUE.equals(
                message.getDeletedByAdmin()
        )) {

            throw new OperationFailException(
                    "Message already removed"
            );
        }

        // SELF REPORT VALIDATION
        if (message.getSender().getId()
                .equals(currentUser.getId())) {

            throw new OperationFailException(
                    "You cannot report your own message"
            );
        }

        // DUPLICATE REPORT VALIDATION
        boolean alreadyReported =
                reportRepository
                        .findByReportedByAndMessage(
                                currentUser,
                                message
                        )
                        .isPresent();

        if (alreadyReported) {

            throw new OperationFailException(
                    "You already reported this message"
            );
        }

        Report report = Report.builder()
                .reportedBy(currentUser)
                .message(message)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);

        // NOTIFY MESSAGE OWNER
        notificationService.createNotification(
                message.getSender().getId(),
                currentUser.getId(),
                currentUser.getUname() +
                " reported your message",
                NotificationType.REPORT,
                null,
                null
        );

        // NOTIFY ADMIN
        userRepository.findByRole(
                Role.ADMIN
        ).ifPresent(admin ->

                notificationService.createNotification(
                        admin.getId(),
                        currentUser.getId(),
                        currentUser.getUname() +
                        " reported a message",
                        NotificationType.REPORT,
                        null,
                        null
                )
        );

        logger.info(
                "Message reported successfully"
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

