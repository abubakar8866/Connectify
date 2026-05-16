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

    // CURSOR PAGINATION
    List<Comment> findByDeletedFalseOrderByIdDesc(
            Pageable pageable
    );

    List<Comment> findByDeletedFalseAndIdLessThanOrderByIdDesc(
            Long cursor,
            Pageable pageable
    );

    // SEARCH + CURSOR
    List<Comment> findByContentContainingIgnoreCaseAndDeletedFalseOrderByIdDesc(
            String keyword,
            Pageable pageable
    );

    List<Comment>
    findByContentContainingIgnoreCaseAndDeletedFalseAndIdLessThanOrderByIdDesc(
            String keyword,
            Long cursor,
            Pageable pageable
    );

}

