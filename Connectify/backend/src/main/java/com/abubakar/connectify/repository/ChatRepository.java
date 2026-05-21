package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRepository
        extends JpaRepository<Chat, Long>,
        JpaSpecificationExecutor<Chat> {

    @Query("""
        SELECT DISTINCT c
        FROM Chat c
        JOIN c.participants p1
        JOIN c.participants p2
        WHERE p1.user.id = :userOneId
        AND p2.user.id = :userTwoId
        AND c.deletedByAdmin = false
    """)
    Optional<Chat> findPrivateChatBetweenUsers(
            Long userOneId,
            Long userTwoId
    );

    // ADMIN
    List<Chat> findAllByOrderByIdDesc(
            Pageable pageable
    );

    List<Chat> findByIdLessThanOrderByIdDesc(
            Long id,
            Pageable pageable
    );

    Long countByDeletedByAdminFalse();

    Long countByDeletedByAdminFalseAndIsActiveTrue();

}

