package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Chat;
import com.abubakar.connectify.entity.ChatParticipant;
import com.abubakar.connectify.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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

    List<ChatParticipant>
    findByUserAndDeletedFalseOrderByChatLastMessageAtDesc(
            User user,
            Pageable pageable
    );

    List<ChatParticipant>
    findByUserAndDeletedFalseAndChatIdLessThanOrderByChatLastMessageAtDesc(
            User user,
            Long chatId,
            Pageable pageable
    );

}

