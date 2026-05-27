package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;

import com.abubakar.connectify.enums.AccountStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>,
        JpaSpecificationExecutor<Post> {

    @EntityGraph(attributePaths = {
            "user",
            "mediaList",
            "hashtags"
    })
    Optional<Post> findWithDetailsById(Long id);

    // ================= PERSONALIZED FEED =================

    @EntityGraph(attributePaths = {
            "user",
            "mediaList",
            "hashtags"
    })
    List<Post>
    findByUserInAndDeletedFalseAndUserDeletedFalseAndUserAccountStatusNotOrderByIdDesc(
            List<User> users,
            AccountStatus accountStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "user",
            "mediaList",
            "hashtags"
    })
    List<Post>
    findByUserInAndDeletedFalseAndUserDeletedFalseAndIdLessThanAndUserAccountStatusNotOrderByIdDesc(
            List<User> users,
            Long cursor,
            AccountStatus accountStatus,
            Pageable pageable
    );

    // ================= USER POSTS =================

    @EntityGraph(attributePaths = {
            "user",
            "mediaList",
            "hashtags"
    })
    List<Post>
    findByUserAndDeletedFalseAndUserDeletedFalseAndUserAccountStatusNotOrderByIdDesc(
            User user,
            AccountStatus accountStatus,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "user",
            "mediaList",
            "hashtags"
    })
    List<Post>
    findByUserAndDeletedFalseAndUserDeletedFalseAndIdLessThanAndUserAccountStatusNotOrderByIdDesc(
            User user,
            Long cursor,
            AccountStatus accountStatus,
            Pageable pageable
    );

    // ================= TRENDING POSTS =================

    List<Post>
    findByDeletedFalseAndUserDeletedFalseAndUserIsActiveTrueAndUserAccountStatusNotOrderByLikeCountDescCommentCountDescIdDesc(
            AccountStatus status,
            Pageable pageable
    );

    List<Post>
    findByDeletedFalseAndUserDeletedFalseAndUserIsActiveTrueAndIdLessThanAndUserAccountStatusNotOrderByLikeCountDescCommentCountDescIdDesc(
            Long cursor,
            AccountStatus status,
            Pageable pageable
    );

    // ================= ANALYTICS =================

    Long countByCreatedAtAfter(
            LocalDateTime time
    );

    Long countByDeletedTrue();

    Long countByUser(
            User user
    );

    Long countByUserAndDeletedFalse(User targetUser);

    // ================= POSTS BY HASHTAG =================
    List<Post>
    findByHashtags_NameAndDeletedFalseAndUserDeletedFalseAndUserIsActiveTrueAndUserAccountStatusNotOrderByIdDesc(
            String hashtagName,
            AccountStatus accountStatus,
            Pageable pageable
    );

    List<Post>
    findByHashtags_NameAndDeletedFalseAndUserDeletedFalseAndUserIsActiveTrueAndIdLessThanAndUserAccountStatusNotOrderByIdDesc(
            String hashtagName,
            Long cursor,
            AccountStatus accountStatus,
            Pageable pageable
    );

}

