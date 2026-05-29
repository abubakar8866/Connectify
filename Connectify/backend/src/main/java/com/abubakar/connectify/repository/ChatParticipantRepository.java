package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Chat;
import com.abubakar.connectify.entity.ChatParticipant;
import com.abubakar.connectify.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository
        extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatAndUser(
            Chat chat,
            User user
    );

    boolean existsByChatAndUser(
            Chat chat,
            User user
    );

    List<ChatParticipant> findByChat(
            Chat chat
    );

    // ================= FETCH IDS =================
    @Query("""
        SELECT cp.id
        FROM ChatParticipant cp
        JOIN cp.chat c
        WHERE cp.user = :user
        AND cp.deleted = false
        AND c.deletedByAdmin = false
        ORDER BY c.lastMessageAt DESC
    """)
    List<Long> findChatParticipantIds(
            User user,
            Pageable pageable
    );

    @Query("""
        SELECT cp.id
        FROM ChatParticipant cp
        JOIN cp.chat c
        WHERE cp.user = :user
        AND cp.deleted = false
        AND c.deletedByAdmin = false
        AND c.id < :chatId
        ORDER BY c.lastMessageAt DESC
    """)
    List<Long> findChatParticipantIdsWithCursor(
            User user,
            Long chatId,
            Pageable pageable
    );

    // ================= FETCH FULL GRAPH =================
    @Query("""
        SELECT DISTINCT cp
        FROM ChatParticipant cp
        JOIN FETCH cp.chat c
        JOIN FETCH c.participants participants
        JOIN FETCH participants.user
        WHERE cp.id IN :ids
    """)
    List<ChatParticipant> findChatsWithParticipants(
            List<Long> ids
    );

}

