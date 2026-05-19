package com.abubakar.connectify.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository
        extends JpaRepository<Comment, Long> {

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

    Long countByDeletedFalse();

    // ADMIN ALL COMMENTS
    List<Comment> findAllByOrderByIdDesc(
            Pageable pageable
    );

    List<Comment> findByIdLessThanOrderByIdDesc(
            Long cursor,
            Pageable pageable
    );

    // RESTORE REQUESTS
    List<Comment>
    findByRestoreRequestedTrueOrderByIdDesc(
            Pageable pageable
    );

    List<Comment>
    findByRestoreRequestedTrueAndIdLessThanOrderByIdDesc(
            Long cursor,
            Pageable pageable
    );

    // SEARCH
    List<Comment>
    findByContentContainingIgnoreCaseOrderByIdDesc(
            String keyword,
            Pageable pageable
    );

    List<Comment>
    findByContentContainingIgnoreCaseAndIdLessThanOrderByIdDesc(
            String keyword,
            Long cursor,
            Pageable pageable
    );

}

