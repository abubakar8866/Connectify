package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Notification;
import com.abubakar.connectify.entity.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // FIRST PAGE
    List<Notification>
    findByReceiverOrderByIdDesc(
            User receiver,
            Pageable pageable
    );

    // NEXT PAGES
    List<Notification>
    findByReceiverAndIdLessThanOrderByIdDesc(
            User receiver,
            Long cursor,
            Pageable pageable
    );

    Long countByReceiverAndIsReadFalse(
            User receiver
    );

    List<Notification>
    findByReceiverAndIsReadFalse(
            User receiver
    );

}

