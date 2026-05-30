package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.RefreshToken;
import com.abubakar.connectify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(
            String token
    );

    Optional<RefreshToken> findByUser(
            User user
    );

    void deleteByUser(
            User user
    );

    long deleteByExpiryDateBefore(
            LocalDateTime dateTime
    );

    @Query("""
        SELECT rt
        FROM RefreshToken rt
        JOIN FETCH rt.user
        WHERE rt.token = :token
    """)
    Optional<RefreshToken> findByTokenWithUser(
            String token
    );

}

