package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.HashtagResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.UserSearchResponse;

import java.util.List;

public interface SearchService {

    List<UserSearchResponse> searchUsers(String keyword);

    List<HashtagResponse> searchHashtags(String keyword);

    List<PostResponse> getTrendingPosts();

    List<UserSearchResponse> getSuggestedUsers();

}
