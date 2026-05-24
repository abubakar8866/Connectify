package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository
        extends JpaRepository<Story, Long> ,
        JpaSpecificationExecutor<Story> {

    List<Story>
    findByDeletedFalseAndExpiresAtAfterAndIsActiveTrueOrderByIdDesc(
            LocalDateTime now,
            Pageable pageable
    );

    List<Story>
    findByDeletedFalseAndExpiresAtAfterAndIsActiveTrueAndIdLessThanOrderByIdDesc(
            LocalDateTime now,
            Long cursor,
            Pageable pageable
    );

    List<Story> findByExpiresAtBefore(
            LocalDateTime now
    );

    // USER ACTIVE STORIES
    List<Story>
    findByUserAndDeletedFalseAndExpiresAtAfterAndIsActiveTrueOrderByIdDesc(
            User user,
            LocalDateTime now,
            Pageable pageable
    );

    List<Story>
    findByUserAndDeletedFalseAndExpiresAtAfterAndIsActiveTrueAndIdLessThanOrderByIdDesc(
            User user,
            LocalDateTime now,
            Long cursor,
            Pageable pageable
    );

    // RESTORE REQUESTS
    List<Story>
    findByRestoreRequestedTrueOrderByIdDesc(
            Pageable pageable
    );

    List<Story>
    findByRestoreRequestedTrueAndIdLessThanOrderByIdDesc(
            Long cursor,
            Pageable pageable
    );

    // ================= ANALYTICS =================

    Long countByDeletedFalse();

    Long countByDeletedTrue();

    Long countByDeletedFalseAndIsActiveTrueAndExpiresAtAfter(
            LocalDateTime now
    );

    Long countByExpiresAtBefore(
            LocalDateTime now
    );

    Long countByRestoreRequestedTrue();

    // TOP VIEWED STORIES
    List<Story>
    findTop10ByDeletedFalseOrderByViewCountDescIdDesc();

    // TOP REACTED STORIES
    List<Story>
    findTop10ByDeletedFalseOrderByReactionCountDescIdDesc();

}

