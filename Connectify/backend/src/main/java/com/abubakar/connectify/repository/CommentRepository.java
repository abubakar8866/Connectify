package com.abubakar.connectify.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.Comment;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository
        extends JpaRepository<Comment, Long>,
        JpaSpecificationExecutor<Comment> {

    List<Comment>
    findByPostIdAndParentCommentIsNullAndDeletedFalseOrderByIdDesc(
            Long postId,
            Pageable pageable
    );

    List<Comment>
    findByPostIdAndParentCommentIsNullAndDeletedFalseAndIdLessThanOrderByIdDesc(
            Long postId,
            Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.parentComment.id IN :parentIds
        AND c.deleted = false
        ORDER BY c.id DESC
    """)
    List<Comment> findRepliesByParentIds(
            @Param("parentIds")
            List<Long> parentIds
    );

    Long countByDeletedFalse();

}

