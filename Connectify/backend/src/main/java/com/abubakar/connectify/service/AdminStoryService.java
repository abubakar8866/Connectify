package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.AdminStoryFilterRequest;
import com.abubakar.connectify.dto.response.AdminStoryResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

public interface AdminStoryService {

    CursorPageResponse<AdminStoryResponse> getAllStories(
            AdminStoryFilterRequest request,
            Long cursor,
            int size
    );

    AdminStoryResponse getStoryById(
            Long storyId
    );

    void deleteStory(Long storyId);

    void restoreStory(Long storyId);

    void approveRestoreRequest(Long storyId);

    void rejectRestoreRequest(Long storyId);

    void expireStory(Long storyId);

    CursorPageResponse<AdminStoryResponse>
    getRestoreRequests(
            Long cursor,
            int size
    );

}

