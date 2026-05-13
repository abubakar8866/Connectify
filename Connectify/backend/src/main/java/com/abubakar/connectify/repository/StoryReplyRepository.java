package com.abubakar.connectify.repository;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.StoryReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryReplyRepository
        extends JpaRepository<StoryReply, Long> {

    List<StoryReply> findByStoryOrderByCreatedAtAsc( Story story );

}

