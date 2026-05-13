package com.abubakar.connectify.repository;

import java.util.List;
import java.util.Optional;

import com.abubakar.connectify.enums.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUname(String uname);

    Boolean existsByEmail(String email);

    Boolean existsByUname(String uname);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByRole(Role role);

    List<User> findByUnameContainingIgnoreCaseOrNameContainingIgnoreCase(
            String uname,
            String name
    );

    @Query("""
        SELECT u FROM User u
        WHERE u.id NOT IN :excludedIds
        ORDER BY u.followersCount DESC
    """)
    List<User> findSuggestedUsers(List<Long> excludedIds);

}

