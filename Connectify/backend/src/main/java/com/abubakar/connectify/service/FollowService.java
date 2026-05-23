package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.CursorCountResponse;
import com.abubakar.connectify.dto.response.FollowCountResponse;
import com.abubakar.connectify.dto.response.FollowResponse;
import com.abubakar.connectify.dto.response.UserPreviewResponse;

public interface FollowService {

    FollowResponse toggleFollow(Long userId);

    CursorCountResponse<UserPreviewResponse> getFollowers(
            Long userId,
            Long cursor,
            int size
    );

    CursorCountResponse<UserPreviewResponse> getFollowing(
            Long userId,
            Long cursor,
            int size
    );

    FollowCountResponse getFollowCounts(
            Long userId
    );

}

