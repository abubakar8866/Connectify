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

    boolean existsByReportedByAndPost(
            User reportedBy,
            Post post
    );

    boolean existsByReportedByAndComment(
            User reportedBy,
            Comment comment
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

    boolean existsByReportedByAndReportedUserAndStatus(
            User reportedBy,
            User reportedUser,
            ReportStatus status
    );

}

