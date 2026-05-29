package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.StoryReaction;
import com.abubakar.connectify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryReactionRepository
        extends JpaRepository<StoryReaction, Long> {

    Optional<StoryReaction> findByStoryAndUser(Story story,User user);

    @Query("""
        SELECT sr.story.id
        FROM StoryReaction sr
        WHERE sr.user.id = :userId
        AND sr.story.id IN :storyIds
    """)
    List<Long> findReactedStoryIds(
            @Param("userId") Long userId,
            @Param("storyIds") List<Long> storyIds
    );

}

