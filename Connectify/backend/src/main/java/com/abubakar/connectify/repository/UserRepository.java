package com.abubakar.connectify.repository;

import java.util.Optional;

import com.abubakar.connectify.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import com.abubakar.connectify.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUname(String uname);

    Boolean existsByEmail(String email);

    Boolean existsByUname(String uname);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByRole(Role role);

}

