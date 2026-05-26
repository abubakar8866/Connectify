package com.abubakar.connectify.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.Comment;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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

    Long countByDeletedFalse();


}

