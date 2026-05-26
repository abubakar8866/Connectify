package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.AdminStoryFilterRequest;
import com.abubakar.connectify.dto.response.AdminStoryResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

public interface AdminStoryService {

    CursorPageResponse<AdminStoryResponse> getStories(
            AdminStoryFilterRequest request,
            Long cursor,
            int size
    );

    AdminStoryResponse getStoryById(
            Long storyId
    );

    // MODERATION
    void moderateStory(
            Long storyId
    );

    // RESTORE
    void approveStoryRestore(
            Long storyId
    );

    void rejectStoryRestore(
            Long storyId
    );

    // FORCE EXPIRE
    void expireStory(
            Long storyId
    );

    // HARD DELETE
    void permanentlyDeleteStory(
            Long storyId
    );

}

