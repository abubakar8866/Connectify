package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.StoryReactionRequest;
import com.abubakar.connectify.dto.request.StoryReplyRequest;
import com.abubakar.connectify.dto.response.StoryResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StoryService {

    StoryResponse createStory( MultipartFile file );

    List<StoryResponse> getActiveStories(
            Long cursor,
            int size
    );

    void viewStory(Long storyId);

    void reactStory( Long storyId,  StoryReactionRequest request );

    void replyStory( Long storyId, StoryReplyRequest request );

    void deleteStory(Long storyId);

    List<UserResponse> getStoryViewers(
            Long storyId,
            Long cursor,
            int size
    );

}

