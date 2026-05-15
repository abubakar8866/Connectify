package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.NotificationResponse;
import com.abubakar.connectify.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    void createNotification(
            Long receiverId,
            Long senderId,
            String message,
            NotificationType type,
            Long postId,
            Long commentId
    );

    List<NotificationResponse> getMyNotifications();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void deleteNotification(Long notificationId);

    Long getUnreadCount();

}

