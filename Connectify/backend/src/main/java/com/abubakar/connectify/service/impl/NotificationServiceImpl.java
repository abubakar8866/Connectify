package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.NotificationResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Notification;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.NotificationRepository;
import com.abubakar.connectify.service.NotificationService;
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
public class NotificationServiceImpl
        implements NotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OwnershipValidator ownershipValidator;

    @Autowired
    private UserAccessValidator userAccessValidator;

    @Autowired
    private PostAccessValidator postAccessValidator;

    @Autowired
    private CommentAccessValidator commentAccessValidator;

    // ================= CREATE NOTIFICATION =================
    @Override
    public void createNotification(
            Long receiverId,
            Long senderId,
            String message,
            NotificationType type,
            Long postId,
            Long commentId
    ) {

        logger.info(
                "Creating notification | receiverId: {} | senderId: {}",
                receiverId,
                senderId
        );

        // DON'T NOTIFY SELF
        if (receiverId.equals(senderId)) {

            logger.info(
                    "Skipping self notification | userId: {}",
                    senderId
            );

            return;
        }

        User receiver = userAccessValidator.getValidUser(receiverId);

        User sender = userAccessValidator.getValidUser(senderId);

        Notification notification =
                Notification.builder()
                        .receiver(receiver)
                        .sender(sender)
                        .message(message)
                        .type(type)
                        .isRead(false)
                        .build();

        if (postId != null) {

            Post post = postAccessValidator.getActivePost(postId);

            notification.setPost(post);
        }

        if (commentId != null) {

            Comment comment = commentAccessValidator.getActiveComment(commentId);

            notification.setComment(comment);
        }

        notificationRepository.save(notification);

        logger.info(
                """
                Notification created successfully
                | receiverId: {}
                | senderId: {}
                | type: {}
                """,
                receiverId,
                senderId,
                type
        );

    }

    // ================= GET MY NOTIFICATIONS =================
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationResponse> getMyNotifications(
            Long cursor,
            int size
    ) {

        User currentUser = authUtil.getCurrentUser();

        logger.info(
                """
                Fetching notifications
                | userId: {}
                | cursor: {}
                | size: {}
                """,
                currentUser.getId(),
                cursor,
                size
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Notification> notifications;

        // FIRST PAGE
        if (cursor == null) {

            notifications =
                    notificationRepository
                            .findByReceiverOrderByIdDesc(
                                    currentUser,
                                    pageable
                            );

        } else {

            // NEXT PAGES
            notifications =
                    notificationRepository
                            .findByReceiverAndIdLessThanOrderByIdDesc(
                                    currentUser,
                                    cursor,
                                    pageable
                            );
        }

        if (notifications.isEmpty()) {

            logger.info(
                    "No notifications found | userId: {}",
                    currentUser.getId()
            );

        }else {

            logger.info(
                    "Notifications fetched successfully | count: {}",
                    notifications.size()
            );

        }

        return CursorPaginationUtil.buildResponse(
                notifications,
                size,
                Notification::getId,
                this::mapToResponse
        );
    }

    // ================= MARK AS READ =================
    @Override
    public void markAsRead(Long notificationId) {

        User currentUser = authUtil.getCurrentUser();

        logger.info(
                "Marking notification as read | notificationId: {}",
                notificationId
        );

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Notification not found"
                                )
                        );

        if (Boolean.TRUE.equals(notification.getIsRead())) {

            logger.warn(
                    """
                    Notification already read
                    | notificationId: {}
                    | userId: {}
                    """,
                    notificationId,
                    currentUser.getId()
            );

            throw new OperationFailException("Notification is already read.");

        }

        // OWNERSHIP CHECK
        logger.debug(
                """
                Validating notification ownership
                | notificationId: {}
                | receiverId: {}
                | currentUserId: {}
                """,
                notification.getId(),
                notification.getReceiver().getId(),
                currentUser.getId()
        );
        this.ownershipValidator.validate(
                notification.getReceiver().getId(),
                currentUser,
                "You are not authorized to access this notification");

        notification.setIsRead(true);

        notificationRepository.save(notification);

        logger.info(
                """
                Notification marked as read successfully
                | notificationId: {}
                | userId: {}
                """,
                notificationId,
                currentUser.getId()
        );

    }

    // ================= MARK ALL AS READ =================
    @Override
    public void markAllAsRead() {

        User currentUser = authUtil.getCurrentUser();

        logger.info(
                "Marking all notifications as read | userId: {}",
                currentUser.getId()
        );

        List<Notification> unreadNotifications =
                notificationRepository
                        .findByReceiverAndIsReadFalse(
                                currentUser
                        );

        if (unreadNotifications.isEmpty()) {

            logger.info(
                    "No unread notifications found | userId: {}",
                    currentUser.getId()
            );

            return;
        }

        logger.info(
                """
                Updating unread notifications
                | userId: {}
                | unreadCount: {}
                """,
                currentUser.getId(),
                unreadNotifications.size()
        );

        unreadNotifications.forEach(
                notification ->
                        notification.setIsRead(true)
        );

        notificationRepository.saveAll(
                unreadNotifications
        );

        logger.info(
                "All notifications marked as read | count: {}",
                unreadNotifications.size()
        );
    }

    // ================= UNREAD COUNT =================
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount() {

        User currentUser = authUtil.getCurrentUser();

        Long unreadCount =
                notificationRepository
                        .countByReceiverAndIsReadFalse(
                                currentUser
                        );

        logger.info(
                """
                Unread notification count fetched
                | userId: {}
                | unreadCount: {}
                """,
                currentUser.getId(),
                unreadCount
        );

        return unreadCount;

    }

    // ================= DELETE NOTIFICATION =================
    @Override
    public void deleteNotification(Long notificationId) {

        User currentUser = authUtil.getCurrentUser();

        logger.info(
                "Deleting notification | notificationId: {}",
                notificationId
        );

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Notification not found"
                                )
                        );

        // OWNERSHIP CHECK
        logger.debug(
                """
                Validating notification delete ownership
                | notificationId: {}
                | receiverId: {}
                | currentUserId: {}
                """,
                notification.getId(),
                notification.getReceiver().getId(),
                currentUser.getId()
        );
        this.ownershipValidator.validate(
                notification.getReceiver().getId(),
                currentUser,
                "You are not authorized to delete this notification."
        );

        notificationRepository.delete(notification);

        logger.info(
                "Notification deleted successfully | notificationId: {}",
                notificationId
        );
    }

    // ================= MAP TO RESPONSE =================
    private NotificationResponse mapToResponse(
            Notification notification
    ) {

        return NotificationResponse.builder()

                .id(notification.getId())

                .type(notification.getType())

                .message(notification.getMessage())

                .isRead(notification.getIsRead())

                .senderId(
                        notification.getSender().getId()
                )

                .senderUsername(
                        notification.getSender().getUname()
                )

                .senderProfileImage(
                        notification.getSender().getProfileImageUrl()
                )

                .postId(
                        notification.getPost() != null
                                ? notification.getPost().getId()
                                : null
                )

                .commentId(
                        notification.getComment() != null
                                ? notification.getComment().getId()
                                : null
                )

                .createdAt(
                        notification.getCreatedAt()
                )

                .build();
    }

}

