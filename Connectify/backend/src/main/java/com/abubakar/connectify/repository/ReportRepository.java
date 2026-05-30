package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.*;

import com.abubakar.connectify.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository
        extends JpaRepository<Report, Long> {

    Long countByReportedUser(User user);

    Long countByPost(Post post);

    @Query("""
        SELECT DISTINCT r.post
        FROM Report r
        WHERE r.post IS NOT NULL
    """)
    Page<Post> findReportedPosts( Pageable pageable );

    Optional<Report> findByReportedByAndPost( User user, Post post );

    Optional<Report> findByReportedByAndComment( User user, Comment comment );

    Optional<Report> findByReportedByAndReportedUser( User reportedBy, User reportedUser );

    List<Report> findByStatus( ReportStatus status );

    Long countByComment(Comment comment);

    List<Report> findByCommentIsNotNull();

    Optional<Report> findByReportedByAndStory(
            User reportedBy,
            Story story
    );

    @Query("""
        SELECT DISTINCT r.comment
        FROM Report r
        WHERE r.comment IS NOT NULL
        AND r.comment.deleted = false
        ORDER BY r.comment.id DESC
    """)
    List<Comment> findReportedComments(
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT r.comment
        FROM Report r
        WHERE r.comment IS NOT NULL
        AND r.comment.deleted = false
        AND r.comment.id < :cursor
        ORDER BY r.comment.id DESC
    """)
    List<Comment> findReportedCommentsByCursor(
            Long cursor,
            Pageable pageable
    );

    Optional<Report> findByReportedByAndChat(
            User reportedBy,
            Chat chat
    );

    Optional<Report> findByReportedByAndMessage(
            User reportedBy,
            Message message
    );

    @Query("""
        SELECT r.post.id, COUNT(r.id)
        FROM Report r
        WHERE r.post.id IN :postIds
        GROUP BY r.post.id
    """)
    List<Object[]> getReportCounts(
            List<Long> postIds
    );

    boolean existsByReportedByAndPost(
            User reportedBy,
            Post post
    );

    boolean existsByReportedByAndComment(
            User reportedBy,
            Comment comment
    );

    boolean existsByReportedByAndReportedUser(
            User reportedBy,
            User reportedUser
    );

    boolean existsByReportedByAndStory(
            User reportedBy,
            Story story
    );

    boolean existsByReportedByAndChat(
            User reportedBy,
            Chat chat
    );

    boolean existsByReportedByAndMessage(
            User reportedBy,
            Message message
    );

    List<Report> findByStatusOrderByIdDesc(
            ReportStatus status,
            Pageable pageable
    );

    List<Report> findByStatusAndIdLessThanOrderByIdDesc(
            ReportStatus status,
            Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT r
        FROM Report r
        JOIN FETCH r.reportedBy
        WHERE r.id = :reportId
    """)
    Optional<Report> findWithReporterById(
            Long reportId
    );

}

