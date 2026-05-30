package com.abubakar.connectify.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Like;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserAndPost(User user,Post post);

    boolean existsByUserAndComment(
            User user,
            Comment comment
    );

    Optional<Like> findByUserAndComment(User user, Comment comment);

    @Query("""
        SELECT l.post.id
        FROM Like l
        WHERE l.user = :user
        AND l.post.id IN :postIds
    """)
    Set<Long> findLikedPostIdsByUserAndPostIds(
            @Param("user") User user,
            @Param("postIds") List<Long> postIds
    );

    @Query("""
        SELECT l.comment.id
        FROM Like l
        WHERE l.user = :user
        AND l.comment.id IN :commentIds
    """)
    Set<Long> findLikedCommentIdsByUserAndCommentIds(
            @Param("user") User user,
            @Param("commentIds") List<Long> commentIds
    );

    @Modifying
    @Query("""
        DELETE FROM Like l
        WHERE l.user = :user
        AND l.post = :post
    """)
    void deleteByUserAndPost(
            @Param("user") User user,
            @Param("post") Post post
    );

}

