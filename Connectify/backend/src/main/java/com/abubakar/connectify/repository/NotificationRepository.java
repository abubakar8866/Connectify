package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Notification;
import com.abubakar.connectify.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByReceiverOrderByCreatedAtDesc(
            User receiver
    );

    Long countByReceiverAndIsReadFalse(
            User receiver
    );

    List<Notification>
    findByReceiverAndIsReadFalse(
            User receiver
    );

}