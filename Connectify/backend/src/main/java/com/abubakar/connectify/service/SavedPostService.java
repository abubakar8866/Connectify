package com.abubakar.connectify.service;

import java.util.List;

import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.SavePostResponse;

public interface SavedPostService {

    SavePostResponse toggleSavePost(Long postId);

    List<PostResponse> getSavedPosts();

}

