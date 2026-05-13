package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository
        extends JpaRepository<Story, Long> {

    List<Story> findByExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime now);

    List<Story> findByUserAndExpiresAtAfterOrderByCreatedAtDesc(User user, LocalDateTime now);

    List<Story> findByExpiresAtBefore(LocalDateTime now);
}

