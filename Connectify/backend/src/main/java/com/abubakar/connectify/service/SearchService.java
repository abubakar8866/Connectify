package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.UserSearchRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.HashtagResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.UserSearchResponse;

public interface SearchService {

    CursorPageResponse<UserSearchResponse> searchUsers(
            UserSearchRequest request,
            Long cursor,
            int size
    );

    CursorPageResponse<HashtagResponse> searchHashtags(
            String keyword,
            Long cursor,
            int size
    );

    CursorPageResponse<PostResponse> getPostsByHashtag(
            String hashtagName,
            Long cursor,
            int size
    );

    CursorPageResponse<PostResponse> getTrendingPosts(
            Long cursor,
            int size
    );

    CursorPageResponse<UserSearchResponse> getSuggestedUsers(
            Long cursor,
            int size
    );

}

