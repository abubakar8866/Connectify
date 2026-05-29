package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.StoryView;
import com.abubakar.connectify.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryViewRepository
        extends JpaRepository<StoryView, Long> {

    boolean existsByStoryAndViewer( Story story, User viewer );

    List<StoryView> findByStoryOrderByIdDesc(
            Story story,
            Pageable pageable
    );

    List<StoryView> findByStoryAndIdLessThanOrderByIdDesc(
            Story story,
            Long cursor,
            Pageable pageable
    );

    @Query("""
        SELECT sv.story.id
        FROM StoryView sv
        WHERE sv.viewer.id = :viewerId
        AND sv.story.id IN :storyIds
    """)
    List<Long> findViewedStoryIds(
            @Param("viewerId") Long viewerId,
            @Param("storyIds") List<Long> storyIds
    );

}

