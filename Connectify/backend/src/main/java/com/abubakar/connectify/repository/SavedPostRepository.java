package com.abubakar.connectify.repository;

import java.util.List;
import java.util.Optional;

import com.abubakar.connectify.enums.AccountStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.SavedPost;
import com.abubakar.connectify.entity.User;

@Repository
public interface SavedPostRepository
        extends JpaRepository<SavedPost, Long> {

    Optional<SavedPost> findByUserAndPost(
            User user,
            Post post
    );

    @EntityGraph(attributePaths = {
            "post",
            "post.user",
            "post.mediaList",
            "post.hashtags"
    })
    List<SavedPost>
    findByUserAndPostDeletedFalseAndPostUserDeletedFalseAndPostUserIsActiveTrueAndPostUserAccountStatusNotOrderByIdDesc(
            User user,
            AccountStatus accountStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "post",
            "post.user",
            "post.mediaList",
            "post.hashtags"
    })
    List<SavedPost>
    findByUserAndPostDeletedFalseAndPostUserDeletedFalseAndPostUserIsActiveTrueAndPostUserAccountStatusNotAndIdLessThanOrderByIdDesc(
            User user,
            AccountStatus accountStatus,
            Long cursor,
            Pageable pageable
    );

}

