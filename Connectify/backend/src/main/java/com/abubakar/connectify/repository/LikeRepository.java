package com.abubakar.connectify.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Like;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndPost(User user, Post post);

    Optional<Like> findByUserAndComment(User user, Comment comment);

}

