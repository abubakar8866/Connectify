package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Notification;
import com.abubakar.connectify.entity.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // FIRST PAGE
    @EntityGraph(attributePaths = {
            "sender",
            "post",
            "comment"
    })
    List<Notification>
    findByReceiverOrderByIdDesc(
            User receiver,
            Pageable pageable
    );

    // NEXT PAGES
    @EntityGraph(attributePaths = {
            "sender",
            "post",
            "comment"
    })
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

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true
        WHERE n.receiver = :receiver
        AND n.isRead = false
    """)
    int markAllAsRead(
            @Param("receiver") User receiver
    );

}

