package com.abubakar.connectify.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends
        JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByUname(String uname);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByRole(Role role);

    Optional<User> findByRole(Role role);

    Long countByIsActiveTrue();

    Long countByAccountStatus(AccountStatus status);

    Long countByCreatedAtAfter(LocalDateTime time);

    Long countByDeletedTrue();

    @Query("""
        SELECT DISTINCT r.reportedUser
        FROM Report r
        WHERE r.reportedUser IS NOT NULL
    """)
    Page<User> findReportedUsers(Pageable pageable);

    @Query("""
        SELECT u
        FROM User u
        ORDER BY
            (u.followersCount + u.followingCount) DESC
    """)
    List<User> findMostActiveUsers();

    List<User>
    findByIdNotInOrderByFollowersCountDesc(
            List<Long> excludedIds,
            Pageable pageable
    );

    List<User>
    findByIdNotInAndIdLessThanOrderByFollowersCountDesc(
            List<Long> excludedIds,
            Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT r.reportedUser
        FROM Report r
        WHERE r.reportedUser IS NOT NULL
        AND r.reportedUser.id < :cursor
        ORDER BY r.reportedUser.id DESC
    """)
    List<User> findReportedUsersByCursor(
            Long cursor,
            Pageable pageable
    );

}

