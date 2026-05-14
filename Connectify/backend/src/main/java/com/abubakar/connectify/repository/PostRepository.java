package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByIdDesc(
            Pageable pageable
    );

    List<Post> findByIdLessThanOrderByIdDesc(
            Long cursor,
            Pageable pageable
    );

    List<Post> findTop20ByOrderByLikeCountDescCommentCountDesc();

    Long countByCreatedAtAfter(LocalDateTime time);

    Long countByDeletedTrue();

    Long countByUser(User user);

    Page<Post> findByCaptionContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM Post p
        WHERE LOWER(p.user.uname)
        LIKE LOWER(CONCAT('%', :username, '%'))
    """)
    Page<Post> searchByUsername(
            String username,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM Post p
        JOIN p.hashtags h
        WHERE LOWER(h.name)
        LIKE LOWER(CONCAT('%', :hashtag, '%'))
    """)
    Page<Post> searchByHashtag(
            String hashtag,
            Pageable pageable
    );

}

