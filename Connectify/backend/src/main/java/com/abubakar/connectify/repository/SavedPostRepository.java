package com.abubakar.connectify.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.SavedPost;
import com.abubakar.connectify.entity.User;

@Repository
public interface SavedPostRepository
        extends JpaRepository<SavedPost, Long> {

    Optional<SavedPost> findByUserAndPost(User user, Post post);

    // FIRST PAGE
    List<SavedPost>
    findByUserOrderByIdDesc(
            User user,
            Pageable pageable
    );

    // CURSOR PAGINATION
    List<SavedPost>
    findByUserAndIdLessThanOrderByIdDesc(
            User user,
            Long cursor,
            Pageable pageable
    );

}

