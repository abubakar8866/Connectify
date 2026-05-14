package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.Report;
import com.abubakar.connectify.entity.User;

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

}