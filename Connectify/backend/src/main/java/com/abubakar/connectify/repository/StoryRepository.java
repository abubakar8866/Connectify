package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository
        extends JpaRepository<Story, Long>,
        JpaSpecificationExecutor<Story> {

    // ================= ACTIVE STORIES =================

    @Query("""
        SELECT s
        FROM Story s
        JOIN FETCH s.user
        WHERE s.deleted = false
        AND s.isActive = true
        AND s.expiresAt > :now
        ORDER BY s.id DESC
    """)
    List<Story> findActiveStories(
            LocalDateTime now,
            Pageable pageable
    );

    @Query("""
        SELECT s
        FROM Story s
        JOIN FETCH s.user
        WHERE s.deleted = false
        AND s.isActive = true
        AND s.expiresAt > :now
        AND s.id < :cursor
        ORDER BY s.id DESC
    """)
    List<Story> findActiveStoriesByCursor(
            LocalDateTime now,
            Long cursor,
            Pageable pageable
    );

    // ================= MY STORIES =================

    @Query("""
        SELECT s
        FROM Story s
        JOIN FETCH s.user
        WHERE s.user = :user
        AND s.deleted = false
        AND s.isActive = true
        AND s.expiresAt > :now
        ORDER BY s.id DESC
    """)
    List<Story> findMyActiveStories(
            User user,
            LocalDateTime now,
            Pageable pageable
    );

    @Query("""
        SELECT s
        FROM Story s
        JOIN FETCH s.user
        WHERE s.user = :user
        AND s.deleted = false
        AND s.isActive = true
        AND s.expiresAt > :now
        AND s.id < :cursor
        ORDER BY s.id DESC
    """)
    List<Story> findMyActiveStoriesByCursor(
            User user,
            LocalDateTime now,
            Long cursor,
            Pageable pageable
    );

    // ================= USER STORIES =================

    @Query("""
        SELECT s
        FROM Story s
        JOIN FETCH s.user
        WHERE s.user = :user
        AND s.deleted = false
        AND s.isActive = true
        AND s.expiresAt > :now
        ORDER BY s.id DESC
    """)
    List<Story> findUserActiveStories(
            User user,
            LocalDateTime now,
            Pageable pageable
    );

    @Query("""
        SELECT s
        FROM Story s
        JOIN FETCH s.user
        WHERE s.user = :user
        AND s.deleted = false
        AND s.isActive = true
        AND s.expiresAt > :now
        AND s.id < :cursor
        ORDER BY s.id DESC
    """)
    List<Story> findUserActiveStoriesByCursor(
            User user,
            LocalDateTime now,
            Long cursor,
            Pageable pageable
    );

    // ================= OTHER =================

    List<Story>
    findByExpiresAtBeforeAndDeletedFalse(
            LocalDateTime now
    );

    Long countByDeletedFalse();

    Long countByDeletedTrue();

    Long countByDeletedFalseAndIsActiveTrueAndExpiresAtAfter(
            LocalDateTime now
    );

    Long countByExpiresAtBeforeAndDeletedFalse(
            LocalDateTime now
    );

    Long countByRestoreRequestedTrue();

    List<Story>
    findTop10ByDeletedFalseOrderByViewCountDescIdDesc();

    List<Story>
    findTop10ByDeletedFalseOrderByReactionCountDescIdDesc();

}

