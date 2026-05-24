package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.NotificationResponse;
import com.abubakar.connectify.enums.NotificationType;

public interface NotificationService {

    void createNotification(
            Long receiverId,
            Long senderId,
            String message,
            NotificationType type,
            Long postId,
            Long commentId
    );

    CursorPageResponse<NotificationResponse>
    getMyNotifications(
            Long cursor,
            int size
    );

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void deleteNotification(Long notificationId);

    Long getUnreadCount();

}

