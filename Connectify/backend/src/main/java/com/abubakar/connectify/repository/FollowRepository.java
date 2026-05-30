package com.abubakar.connectify.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.Follow;
import com.abubakar.connectify.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(
            User follower,
            User following
    );

    List<Follow> findByFollower(User follower);

    @Query("""
        SELECT f
        FROM Follow f
        JOIN FETCH f.follower
        WHERE f.following = :following
        ORDER BY f.id DESC
    """)
    List<Follow> findFollowers(
            @Param("following") User following,
            Pageable pageable
    );

    @Query("""
        SELECT f
        FROM Follow f
        JOIN FETCH f.follower
        WHERE f.following = :following
        AND f.id < :cursor
        ORDER BY f.id DESC
    """)
    List<Follow> findFollowersByCursor(
            @Param("following") User following,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT f
        FROM Follow f
        JOIN FETCH f.following
        WHERE f.follower = :follower
        ORDER BY f.id DESC
    """)
    List<Follow> findFollowing(
            @Param("follower") User follower,
            Pageable pageable
    );

    @Query("""
        SELECT f
        FROM Follow f
        JOIN FETCH f.following
        WHERE f.follower = :follower
        AND f.id < :cursor
        ORDER BY f.id DESC
    """)
    List<Follow> findFollowingByCursor(
            @Param("follower") User follower,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT f.following.id
        FROM Follow f
        WHERE f.follower = :follower
        AND f.following.id IN :userIds
    """)
    Set<Long> findFollowingIds(
            @Param("follower") User follower,
            @Param("userIds") List<Long> userIds
    );

    @Query("""
        SELECT f.following.id
        FROM Follow f
        WHERE f.follower = :follower
    """)
    List<Long> findFollowingIdsByFollower(
            @Param("follower") User follower
    );

}

