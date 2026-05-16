package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.HashtagResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.UserSearchResponse;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;

import java.util.List;

public interface SearchService {

    List<UserSearchResponse> searchUsers(

            String keyword,
            Boolean verified,
            Boolean isPrivate,
            Boolean active,
            AccountStatus status,
            String city,
            Gender gender,
            Long minFollowers,

            Long cursor,
            int size
    );

    List<HashtagResponse> searchHashtags(
            String keyword,
            Long cursor,
            int size
    );

    List<PostResponse> getTrendingPosts(
            Long cursor,
            int size
    );

    List<UserSearchResponse> getSuggestedUsers(
            Long cursor,
            int size
    );

}

