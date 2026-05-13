package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.StoryReaction;
import com.abubakar.connectify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryReactionRepository
        extends JpaRepository<StoryReaction, Long> {

    Optional<StoryReaction> findByStoryAndUser(Story story,User user);

}

