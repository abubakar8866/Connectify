package com.abubakar.connectify.service;

import java.util.List;

import com.abubakar.connectify.dto.response.FollowResponse;
import com.abubakar.connectify.dto.response.UserPreviewResponse;

public interface FollowService {

    FollowResponse toggleFollow(Long userId);

    List<UserPreviewResponse> getFollowers(Long userId);

    List<UserPreviewResponse> getFollowing(Long userId);

}

