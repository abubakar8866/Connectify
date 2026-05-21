package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Chat;
import com.abubakar.connectify.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository
        extends JpaRepository<Message, Long>,
        JpaSpecificationExecutor<Message> {

    // ================= GET MESSAGES =================

    @Query("""
        SELECT m
        FROM Message m
        WHERE m.chat = :chat
        AND m.deletedByAdmin = false
        AND m.chat.deletedByAdmin = false
        AND :userId NOT IN (
            SELECT u.id
            FROM m.deletedForUsers u
        )
        ORDER BY m.id DESC
    """)
    List<Message> findVisibleMessages(
            Chat chat,
            Long userId,
            Pageable pageable
    );

    @Query("""
        SELECT m
        FROM Message m
        WHERE m.chat = :chat
        AND m.id < :cursor
        AND m.deletedByAdmin = false
        AND m.chat.deletedByAdmin = false
        AND :userId NOT IN (
            SELECT u.id
            FROM m.deletedForUsers u
        )
        ORDER BY m.id DESC
    """)
    List<Message> findVisibleMessagesWithCursor(
            Chat chat,
            Long cursor,
            Long userId,
            Pageable pageable
    );

    // ================= UNSEEN =================

    List<Message>
    findByChatAndSenderIdNotAndIsSeenFalseAndDeletedByAdminFalse(
            Chat chat,
            Long senderId
    );

    List<Message>
    findByChatAndDeletedByAdminFalseOrderByIdDesc(
            Chat chat,
            Pageable pageable
    );

    List<Message>
    findByChatAndDeletedByAdminFalseAndIdLessThanOrderByIdDesc(
            Chat chat,
            Long id,
            Pageable pageable
    );

    List<Message> findByChatOrderByIdDesc(
            Chat chat,
            Pageable pageable
    );

    List<Message> findByChatAndIdLessThanOrderByIdDesc(
            Chat chat,
            Long id,
            Pageable pageable
    );

    Long countByCreatedAtAfterAndDeletedByAdminFalse(
            LocalDateTime time
    );

}

