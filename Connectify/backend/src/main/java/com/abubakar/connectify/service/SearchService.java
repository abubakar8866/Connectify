package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.HashtagResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.UserSearchResponse;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;

public interface SearchService {

    CursorPageResponse<UserSearchResponse> searchUsers(

            String keyword,
            Boolean verified,
            Boolean emailVerified,
            Boolean isPrivate,
            String city,
            Gender gender,
            Long minFollowers,

            Long cursor,
            int size
    );

    CursorPageResponse<HashtagResponse> searchHashtags(
            String keyword,
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

