package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.CreateReportRequest;
import com.abubakar.connectify.dto.response.ReportResponse;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.enums.ReportStatus;
import com.abubakar.connectify.enums.Role;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.*;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.service.ReportService;

import com.abubakar.connectify.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
    private UserRepository userRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserAccessValidator userAccessValidator;

    @Autowired
    private PostAccessValidator postAccessValidator;

    @Autowired
    private StoryAccessValidator storyAccessValidator;

    @Autowired
    private CommentAccessValidator commentAccessValidator;

    @Autowired
    private ChatAccessValidator chatAccessValidator;

    @Autowired
    private MessageAccessValidator messageAccessValidator;

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

        logger.info(
                "Reporting post | postId: {} | reporterId: {}",
                postId,
                currentUser.getId()
        );

        Post post = postAccessValidator.getActivePost(postId);

        // SELF REPORT VALIDATION
        if (post.getUser().getId()
                .equals(currentUser.getId())) {

            logger.warn(
                    "Self report blocked | reporterId: {} | postId: {}",
                    currentUser.getId(),
                    postId
            );

            throw new OperationFailException(
                    "You cannot report your own post"
            );
        }

        boolean alreadyReported =
                reportRepository
                        .existsByReportedByAndPost(
                                currentUser,
                                post
                        );

        if (alreadyReported) {

            logger.warn(
                    "Duplicate post report blocked | reporterId: {} | postId: {}",
                    currentUser.getId(),
                    postId
            );

            throw new OperationFailException(
                    "You already reported this post"
            );
        }

        try{

            Report report = Report.builder()
                    .reportedBy(currentUser)
                    .post(post)
                    .reason(request.getReason())
                    .description(request.getDescription())
                    .status(ReportStatus.PENDING)
                    .build();

            reportRepository.saveAndFlush(report);

            // NOTIFY POST OWNER
            notificationService.createNotification(
                    post.getUser().getId(),                 // receiver
                    currentUser.getId(),                   // sender
                    currentUser.getUname() +
                            " reported your post",
                    NotificationType.POST_REPORTED,
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
                            NotificationType.POST_REPORTED,
                            post.getId(),
                            null
                    )
            );

            logger.info(
                    "Post reported successfully | postId: {} | reporterId: {}",
                    postId,
                    currentUser.getId()
            );

            return mapToResponse(report);

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    "Duplicate post report prevented by database | reporterId: {} | postId: {}",
                    currentUser.getId(),
                    postId
            );

            throw new OperationFailException(
                    "You already reported this post"
            );
        }

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

        logger.info(
                "Reporting comment | commentId: {} | reporterId: {}",
                commentId,
                currentUser.getId()
        );

        Comment comment = commentAccessValidator.getActiveComment(commentId);

        // SELF REPORT VALIDATION
        if (comment.getUser().getId()
                .equals(currentUser.getId())) {

            logger.warn(
                    "Self comment report blocked | reporterId: {} | commentId: {}",
                    currentUser.getId(),
                    commentId
            );

            throw new OperationFailException(
                    "You cannot report your own comment"
            );
        }

        boolean alreadyReported =
                reportRepository
                        .existsByReportedByAndComment(
                                currentUser,
                                comment
                        );

        if (alreadyReported) {

            logger.warn(
                    "Duplicate comment report blocked | reporterId: {} | commentId: {}",
                    currentUser.getId(),
                    commentId
            );

            throw new OperationFailException(
                    "You already reported this comment"
            );
        }

        try{

            Report report = Report.builder()
                    .reportedBy(currentUser)
                    .comment(comment)
                    .reason(request.getReason())
                    .description(request.getDescription())
                    .status(ReportStatus.PENDING)
                    .build();

            reportRepository.saveAndFlush(report);

            // NOTIFY COMMENT OWNER
            notificationService.createNotification(
                    comment.getUser().getId(),
                    currentUser.getId(),
                    currentUser.getUname() +
                            " reported your comment",
                    NotificationType.COMMENT_REPORTED,
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
                            NotificationType.COMMENT_REPORTED,
                            comment.getPost().getId(),
                            comment.getId()
                    )
            );

            logger.info(
                    "Comment reported successfully | commentId: {} | reporterId: {}",
                    commentId,
                    currentUser.getId()
            );

            return mapToResponse(report);

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    "Duplicate comment report prevented by database | reporterId: {} | commentId: {}",
                    currentUser.getId(),
                    commentId
            );

            throw new OperationFailException(
                    "You already reported this comment."
            );
        }

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

        User user = this.userAccessValidator.getValidUser(userId);
        userAccessValidator.validateActiveUser(user);

        // ADMIN REPORT VALIDATION
        if (user.getRole() == Role.ADMIN) {

            logger.warn(
                    "Admin report blocked | reporterId: {} | targetUserId: {}",
                    currentUser.getId(),
                    userId
            );

            throw new OperationFailException(
                    "Admin accounts cannot be reported"
            );
        }

        if (user.getId().equals(currentUser.getId())) {

            logger.warn(
                    "Duplicate user report blocked | reporterId: {} | targetUserId: {}",
                    currentUser.getId(),
                    userId
            );

            logger.warn(
                    "Self report blocked | userId: {}",
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "You cannot report yourself"
            );
        }

        boolean alreadyReported =
                reportRepository
                        .existsByReportedByAndReportedUser(
                                currentUser,
                                user
                        );

        if (alreadyReported) {

            logger.warn(
                    "You already reported this user | reporterId: {} | userId: {}",
                    currentUser.getId(),
                    userId
            );

            throw new OperationFailException(
                    "You already reported this user"
            );
        }

        try{

            Report report = Report.builder()
                    .reportedBy(currentUser)
                    .reportedUser(user)
                    .reason(request.getReason())
                    .description(request.getDescription())
                    .status(ReportStatus.PENDING)
                    .build();

            reportRepository.saveAndFlush(report);

            // NOTIFY REPORTED USER
            notificationService.createNotification(
                    user.getId(),
                    currentUser.getId(),
                    currentUser.getUname() +
                            " reported your profile",
                    NotificationType.USER_REPORTED,
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
                            NotificationType.USER_REPORTED,
                            null,
                            null
                    )
            );

            logger.info(
                    "User reported successfully | targetUserId: {} | reporterId: {}",
                    userId,
                    currentUser.getId()
            );

            return mapToResponse(report);

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    "Duplicate user report prevented by database | reporterId: {} | userId: {}",
                    currentUser.getId(),
                    userId
            );

            throw new OperationFailException(
                    "You already reported this user."
            );
        }

    }

    @Override
    public ReportResponse reportChat(
            Long chatId,
            CreateReportRequest request
    ) {

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Reporting chat | chatId: {} | reporterId: {}",
                chatId,
                currentUser.getId()
        );

        Chat chat = chatAccessValidator.getActiveChat(chatId);

        boolean participant =
                chatParticipantRepository
                        .existsByChatAndUser(
                                chat,
                                currentUser
                        );

        if (!participant) {

            logger.warn(
                    "Unauthorized chat report blocked | reporterId: {} | chatId: {}",
                    currentUser.getId(),
                    chatId
            );

            throw new OperationFailException(
                    "You are not part of this chat"
            );
        }

        boolean alreadyReported =
                reportRepository
                        .existsByReportedByAndChat(
                                currentUser,
                                chat
                        );

        if (alreadyReported) {

            logger.warn(
                    "You already reported this chat blocked | reporterId: {} | chatId: {}",
                    currentUser.getId(),
                    chatId
            );

            throw new OperationFailException(
                    "You already reported this chat"
            );
        }

        try{

            Report report =
                    Report.builder()
                            .reportedBy(currentUser)
                            .chat(chat)
                            .reason(request.getReason())
                            .description(request.getDescription())
                            .status(ReportStatus.PENDING)
                            .build();

            reportRepository.saveAndFlush(report);

            // NOTIFY ADMIN
            userRepository.findByRole(
                    Role.ADMIN
            ).ifPresent(admin ->

                    notificationService.createNotification(
                            admin.getId(),
                            currentUser.getId(),
                            currentUser.getUname()
                                    + " reported a chat",
                            NotificationType.CHAT_REPORTED,
                            null,
                            null
                    )
            );

            logger.info(
                    "Chat reported successfully | chatId: {} | reporterId: {}",
                    chatId,
                    currentUser.getId()
            );

            return mapToResponse(report);

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    "Duplicate chat report prevented by database | reporterId: {} | chatId: {}",
                    currentUser.getId(),
                    chatId
            );

            throw new OperationFailException(
                    "You already reported this chat."
            );
        }

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

        Message message = messageAccessValidator.getActiveMessage(messageId);

        Chat chat = message.getChat();

        // USER MUST BELONG TO CHAT
        boolean participantExists =
                chatParticipantRepository
                        .existsByChatAndUser(
                                chat,
                                currentUser
                        );

        if (!participantExists) {

            logger.warn(
                    "Unauthorized message report blocked | reporterId: {} | messageId: {}",
                    currentUser.getId(),
                    messageId
            );

            throw new OperationFailException(
                    "You are not part of this chat"
            );
        }

        // SELF REPORT VALIDATION
        if (message.getSender().getId()
                .equals(currentUser.getId())) {

            logger.warn(
                    "Self message report blocked | reporterId: {} | messageId: {}",
                    currentUser.getId(),
                    messageId
            );

            throw new OperationFailException(
                    "You cannot report your own message"
            );
        }

        boolean alreadyReported =
                reportRepository
                        .existsByReportedByAndMessage(
                                currentUser,
                                message
                        );

        if (alreadyReported) {

            logger.warn(
                    "Duplicate message report blocked | reporterId: {} | messageId: {}",
                    currentUser.getId(),
                    messageId
            );

            throw new OperationFailException(
                    "You already reported this message"
            );
        }

        try{

            Report report = Report.builder()
                    .reportedBy(currentUser)
                    .message(message)
                    .reason(request.getReason())
                    .description(request.getDescription())
                    .status(ReportStatus.PENDING)
                    .build();

            reportRepository.saveAndFlush(report);

            // NOTIFY MESSAGE OWNER
            notificationService.createNotification(
                    message.getSender().getId(),
                    currentUser.getId(),
                    currentUser.getUname() +
                            " reported your message",
                    NotificationType.MESSAGE_REPORTED,
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
                            NotificationType.MESSAGE_REPORTED,
                            null,
                            null
                    )
            );

            logger.info(
                    "Message reported successfully | messageId: {} | reporterId: {}",
                    messageId,
                    currentUser.getId()
            );

            return mapToResponse(report);

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    "Duplicate message report prevented by database | reporterId: {} | messageId: {}",
                    currentUser.getId(),
                    messageId
            );

            throw new OperationFailException(
                    "You already reported this message."
            );
        }

    }

    @Override
    public ReportResponse reportStory(
            Long storyId,
            CreateReportRequest request
    ) {

        logger.info(
                "Reporting story | storyId: {}",
                storyId
        );

        User currentUser =
                authUtil.getCurrentUser();

        Story story = storyAccessValidator.getActiveStory(storyId);

        // SELF REPORT VALIDATION
        if (story.getUser().getId()
                .equals(currentUser.getId())) {

            logger.warn(
                    "Self report blocked | userId: {} | storyId: {}",
                    currentUser.getId(),
                    storyId
            );

            throw new OperationFailException(
                    "You cannot report your own story"
            );
        }

        boolean alreadyReported =
                reportRepository
                        .existsByReportedByAndStory(
                                currentUser,
                                story
                        );

        if (alreadyReported) {

            logger.warn(
                    "Duplicate story report blocked | userId: {} | storyId: {}",
                    currentUser.getId(),
                    storyId
            );

            throw new OperationFailException(
                    "You already reported this story"
            );
        }

        try{

            Report report =
                    Report.builder()
                            .reportedBy(currentUser)
                            .story(story)
                            .reason(request.getReason())
                            .description(request.getDescription())
                            .status(ReportStatus.PENDING)
                            .build();

            reportRepository.saveAndFlush(report);

            // NOTIFY STORY OWNER
            notificationService.createNotification(
                    story.getUser().getId(),
                    currentUser.getId(),
                    currentUser.getUname()
                            + " reported your story",
                    NotificationType.STORY_REPORTED,
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
                            currentUser.getUname()
                                    + " reported a story",
                            NotificationType.STORY_REPORTED,
                            null,
                            null
                    )
            );

            logger.info(
                    "Story reported successfully | storyId: {} | reporterId: {}",
                    storyId,
                    currentUser.getId()
            );

            return mapToResponse(report);

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    "Duplicate story report prevented by database | reporterId: {} | storyId: {}",
                    currentUser.getId(),
                    storyId
            );

            throw new OperationFailException(
                    "You already reported this story."
            );
        }

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

                .storyId(
                        report.getStory() != null
                                ? report.getStory().getId()
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

