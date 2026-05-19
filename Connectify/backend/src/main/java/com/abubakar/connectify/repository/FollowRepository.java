package com.abubakar.connectify.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.Follow;
import com.abubakar.connectify.entity.User;
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

}

