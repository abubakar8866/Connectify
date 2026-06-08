package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.*;

import com.abubakar.connectify.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository
        extends JpaRepository<Report, Long>,
        JpaSpecificationExecutor<Report> {

    Long countByReportedUser(User user);

    Long countByStatus(
            ReportStatus status
    );

    Long countByCreatedAtAfter(
            LocalDateTime time
    );

    Long countByComment(Comment comment);

    @Query("""
        SELECT r.post.id, COUNT(r.id)
        FROM Report r
        WHERE r.post.id IN :postIds
        GROUP BY r.post.id
    """)
    List<Object[]> getReportCounts(
            List<Long> postIds
    );

    boolean existsByReportedByAndPostAndStatus(
            User reportedBy,
            Post post,
            ReportStatus status
    );

    boolean existsByReportedByAndCommentAndStatus(
            User reportedBy,
            Comment comment,
            ReportStatus status
    );

    boolean existsByReportedByAndReportedUserAndStatus(
            User reportedBy,
            User reportedUser,
            ReportStatus status
    );

    boolean existsByReportedByAndStoryAndStatus(
            User reportedBy,
            Story story,
            ReportStatus status
    );

    boolean existsByReportedByAndChatAndStatus(
            User reportedBy,
            Chat chat,
            ReportStatus status
    );

    boolean existsByReportedByAndMessageAndStatus(
            User reportedBy,
            Message message,
            ReportStatus status
    );

    @Query("""
        SELECT r
        FROM Report r
        JOIN FETCH r.reportedBy
        LEFT JOIN FETCH r.resolvedBy
        WHERE r.id = :reportId
    """)
    Optional<Report> findWithDetailsById(
            Long reportId
    );

    List<Report> findByReportedUser(
            User user
    );

    List<Report> findByReportedBy(
            User user
    );

    List<Report> findByResolvedBy(
            User user
    );

}

