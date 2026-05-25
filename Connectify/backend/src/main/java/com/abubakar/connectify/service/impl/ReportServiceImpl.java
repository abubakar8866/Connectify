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
import com.abubakar.connectify.util.UserAccessValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
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
    private StoryRepository storyRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserAccessValidator userAccessValidator;

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

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Report failed | post not found | postId: {}",
                            postId
                    );

                    return new ResourceNotFound(
                            "Post not found with id = "+postId
                    );
                });

        // DELETED POST VALIDATION
        if (Boolean.TRUE.equals(post.getDeleted())) {

            logger.warn(
                    "Report failed | deleted post | postId: {}",
                    postId
            );

            throw new OperationFailException(
                    "Deleted post cannot be reported"
            );
        }

        // BANNED OWNER VALIDATION
        if (post.getUser().getAccountStatus()
                == AccountStatus.BANNED) {

            logger.warn(
                    "Report failed | banned post owner | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "This post owner is banned"
            );
        }

        // DELETED OWNER VALIDATION
        if (Boolean.TRUE.equals(post.getUser().getDeleted())) {

            logger.warn(
                    "Report failed | deleted post owner | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "This post owner is deleted"
            );
        }

        // ACTIVE OWNER VALIDATION
        if (Boolean.FALSE.equals(post.getUser().getIsActive())) {

            logger.warn(
                    "Report failed | inactive post owner | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "This post owner is not active"
            );
        }

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
                        .findByReportedByAndPost(
                                currentUser,
                                post
                        )
                        .isPresent();

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

        Comment comment =
                commentRepository.findById(commentId)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Report failed | comment not found | commentId: {}",
                                    commentId
                            );

                            return new ResourceNotFound(
                                    "Post not found with id = "+commentId
                            );
                        });

        // DELETED COMMENT VALIDATION
        if (Boolean.TRUE.equals(comment.getDeleted())) {

            logger.warn(
                    "Report failed | deleted comment | commentId: {}",
                    commentId
            );

            throw new OperationFailException(
                    "Deleted comment cannot be reported"
            );
        }

        // BANNED OWNER VALIDATION
        if (comment.getUser().getAccountStatus()
                == AccountStatus.BANNED) {

            logger.warn(
                    "Report failed | banned comment owner | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getUser().getId()
            );

            throw new OperationFailException(
                    "This comment owner is banned."
            );
        }

        // DELETE OWNER VALIDATION
        if (Boolean.TRUE.equals(comment.getUser().getDeleted())) {

            logger.warn(
                    "Report failed | deleted comment owner | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getUser().getId()
            );

            throw new OperationFailException(
                    "This comment owner is deleted."
            );
        }

        // ACTIVE OWNER VALIDATION
        if (Boolean.FALSE.equals(comment.getUser().getIsActive())) {

            logger.warn(
                    "Report failed | inactive comment owner | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getUser().getId()
            );

            throw new OperationFailException(
                    "This comment owner is not active"
            );
        }

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
                        .findByReportedByAndComment(
                                currentUser,
                                comment
                        )
                        .isPresent();

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
                        .findByReportedByAndReportedUser(
                                currentUser,
                                user
                        )
                        .isPresent();

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

        Chat chat =
                chatRepository.findById(chatId)
                        .orElseThrow(() ->{

                            logger.info(
                                    "Reporting chat | chatId: {} | reporterId: {}",
                                    chatId,
                                    currentUser.getId()
                            );

                            return new ResourceNotFound(
                                    "Chat not found"
                            );

                        });

        if (Boolean.TRUE.equals(chat.getDeletedByAdmin())) {

            logger.info(
                    "Chat is already removed | chatId: {} | Deleted: {}",
                    chatId,
                    chat.getDeletedByAdmin()
            );

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
                        .findByReportedByAndChat(
                                currentUser,
                                chat
                        )
                        .isPresent();

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

        Report report =
                Report.builder()
                        .reportedBy(currentUser)
                        .chat(chat)
                        .reason(request.getReason())
                        .description(request.getDescription())
                        .status(ReportStatus.PENDING)
                        .build();

        reportRepository.save(report);

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

            logger.warn(
                    "Report failed | chat missing for message | messageId: {}",
                    messageId
            );

            throw new OperationFailException(
                    "Chat not found"
            );
        }

        // CHAT DELETED VALIDATION
        if (Boolean.TRUE.equals(chat.getDeletedByAdmin())) {

            logger.warn(
                    "Report failed | deleted chat for message | messageId: {} | chatId: {}",
                    messageId,
                    chat.getId()
            );

            throw new OperationFailException(
                    "Chat is unavailable"
            );
        }

        // BANNED OWNER VALIDATION
        if (message.getSender().getAccountStatus()
                == AccountStatus.BANNED) {

            logger.warn(
                    "Report failed | banned message sender | messageId: {} | senderId: {}",
                    messageId,
                    message.getSender().getId()
            );

            throw new OperationFailException(
                    "This message owner is banned."
            );
        }

        // DELETE OWNER VALIDATION
        if (Boolean.TRUE.equals(message.getSender().getDeleted())) {

            logger.warn(
                    "Report failed | deleted message sender | messageId: {} | senderId: {}",
                    messageId,
                    message.getSender().getId()
            );

            throw new OperationFailException(
                    "This message owner is deleted."
            );
        }

        // ACTIVE OWNER VALIDATION
        if (Boolean.FALSE.equals(message.getSender().getIsActive())) {

            logger.warn(
                    "Report failed | inactive message sender | messageId: {} | senderId: {}",
                    messageId,
                    message.getSender().getId()
            );

            throw new OperationFailException(
                    "This message owner is not active"
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

            logger.warn(
                    "Unauthorized message report blocked | reporterId: {} | messageId: {}",
                    currentUser.getId(),
                    messageId
            );

            throw new OperationFailException(
                    "You are not part of this chat"
            );
        }

        // MESSAGE DELETED VALIDATION
        if (Boolean.TRUE.equals(
                message.getDeletedByAdmin()
        )) {

            logger.warn(
                    "Report failed | deleted message | messageId: {}",
                    messageId
            );

            throw new OperationFailException(
                    "Message already removed"
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

        // DUPLICATE REPORT VALIDATION
        boolean alreadyReported =
                reportRepository
                        .findByReportedByAndMessage(
                                currentUser,
                                message
                        )
                        .isPresent();

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

        Story story =
                storyRepository.findById(storyId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Story not found"
                                )
                        );

        // DELETED STORY VALIDATION
        if (Boolean.TRUE.equals(
                story.getDeleted()
        )) {

            logger.warn(
                    "Report failed | deleted story | storyId: {}",
                    storyId
            );

            throw new OperationFailException(
                    "Deleted story cannot be reported"
            );
        }

        // STORY OWNER BANNED
        if (story.getUser().getAccountStatus()
                == AccountStatus.BANNED) {

            logger.warn(
                    "Report failed | banned story owner | storyId: {}",
                    storyId
            );

            throw new OperationFailException(
                    "This story is unavailable"
            );
        }

        // STORY OWNER DELETED
        if (Boolean.TRUE.equals(
                story.getUser().getDeleted()
        )) {

            logger.warn(
                    "Report failed | deleted story owner | storyId: {}",
                    storyId
            );

            throw new OperationFailException(
                    "This story is unavailable"
            );
        }

        // STORY OWNER INACTIVE
        if (Boolean.FALSE.equals(
                story.getUser().getIsActive()
        )) {

            logger.warn(
                    "Report failed | inactive story owner | storyId: {}",
                    storyId
            );

            throw new OperationFailException(
                    "This story is unavailable"
            );
        }

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

        // DUPLICATE REPORT VALIDATION
        boolean alreadyReported =
                reportRepository
                        .findByReportedByAndStory(
                                currentUser,
                                story
                        )
                        .isPresent();

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

        Report report =
                Report.builder()
                        .reportedBy(currentUser)
                        .story(story)
                        .reason(request.getReason())
                        .description(request.getDescription())
                        .status(ReportStatus.PENDING)
                        .build();

        reportRepository.save(report);

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

