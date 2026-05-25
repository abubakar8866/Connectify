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

    List<Follow> findByFollowing(User following);

    List<Follow>
    findByFollowingOrderByIdDesc(
            User following,
            Pageable pageable
    );

    List<Follow>
    findByFollowingAndIdLessThanOrderByIdDesc(
            User following,
            Long cursor,
            Pageable pageable
    );

    List<Follow>
    findByFollowerOrderByIdDesc(
            User follower,
            Pageable pageable
    );

    List<Follow>
    findByFollowerAndIdLessThanOrderByIdDesc(
            User follower,
            Long cursor,
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

