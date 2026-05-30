package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.NotificationResponse;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.PaginationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    NotificationController.class
            );

    @GetMapping
    public ResponseEntity<
            CursorPageResponse<NotificationResponse>
            > getMyNotifications(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                "GET /api/notifications | cursor: {} | size: {}",
                cursor,
                size
        );

        return ResponseEntity.ok(

                notificationService
                        .getMyNotifications(
                                cursor,
                                size
                        )
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Long id
    ) {

        logger.info(
                "PUT /api/notifications/{}/read",
                id
        );

        notificationService.markAsRead(id);

        return ResponseEntity.ok(
                "Notification marked as read"
        );
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead() {

        logger.info(
                "PUT /api/notifications/read-all"
        );

        notificationService.markAllAsRead();

        return ResponseEntity.ok(
                "All notifications marked as read"
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable Long id
    ) {

        logger.info(
                "DELETE /api/notifications/{}",
                id
        );

        notificationService.deleteNotification(id);

        return ResponseEntity.ok(
                "Notification deleted successfully"
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {

        logger.info(
                "GET /api/notifications/unread-count"
        );

        return ResponseEntity.ok(
                notificationService.getUnreadCount()
        );

    }

}

