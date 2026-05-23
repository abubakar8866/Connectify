package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.StoryReactionRequest;
import com.abubakar.connectify.dto.request.StoryReplyRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.StoryResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StoryService {

    StoryResponse createStory( MultipartFile file);

    CursorPageResponse<StoryResponse> getActiveStories(
            Long cursor,
            int size
    );

    void viewStory(Long storyId);

    void reactStory( Long storyId,  StoryReactionRequest request );

    void replyStory( Long storyId, StoryReplyRequest request );

    void deleteStory(Long storyId);

    CursorPageResponse<UserResponse> getStoryViewers(
            Long storyId,
            Long cursor,
            int size
    );

    CursorPageResponse<StoryResponse> getMyStories(
            Long cursor,
            int size
    );

    CursorPageResponse<StoryResponse>
    getUserActiveStories(
            Long userId,
            Long cursor,
            int size
    );

    void requestRestoreStory(
            Long storyId
    );

}

