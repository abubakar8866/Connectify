package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.Notification;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.NotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationAccessValidator {

    @Autowired
    private NotificationRepository notificationRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    NotificationAccessValidator.class
            );

    public Notification getNotification(
            Long notificationId
    ) {

        logger.debug(
                "Fetching notification | notificationId: {}",
                notificationId
        );

        return notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Notification not found | notificationId: {}",
                            notificationId
                    );

                    return new ResourceNotFound(
                            "Notification not found with id: "
                                    + notificationId
                    );
                });

    }

}

