package com.abubakar.connectify.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.SavedPost;
import com.abubakar.connectify.entity.User;

@Repository
public interface SavedPostRepository
        extends JpaRepository<SavedPost, Long> {

    Optional<SavedPost> findByUserAndPost(User user, Post post);

    List<SavedPost> findByUserOrderByCreatedAtDesc(User user);

}

