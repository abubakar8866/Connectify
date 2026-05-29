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
        LEFT JOIN FETCH m.sender
        LEFT JOIN FETCH m.replyToMessage rm
        LEFT JOIN FETCH rm.sender
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
        LEFT JOIN FETCH m.sender
        LEFT JOIN FETCH m.replyToMessage rm
        LEFT JOIN FETCH rm.sender
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

    Long countByCreatedAtAfterAndDeletedByAdminFalse(
            LocalDateTime time
    );

    @Query("""
        SELECT COUNT(m) > 0
        FROM Message m
        JOIN m.deletedForUsers u
        WHERE m.id = :messageId
        AND u.id = :userId
    """)
    boolean isMessageDeletedForUser(
            Long messageId,
            Long userId
    );

}

