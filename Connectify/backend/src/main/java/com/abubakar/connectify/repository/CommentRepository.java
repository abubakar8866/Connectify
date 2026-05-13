package com.abubakar.connectify.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(Long postId);

    List<Comment> findByParentCommentId(Long parentId);
}

