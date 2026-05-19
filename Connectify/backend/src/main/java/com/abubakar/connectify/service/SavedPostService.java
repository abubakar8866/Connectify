package com.abubakar.connectify.service;

import java.util.List;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.SavePostResponse;

public interface SavedPostService {

    SavePostResponse toggleSavePost(Long postId);

    // CURSOR PAGINATION
    CursorPageResponse<PostResponse> getSavedPosts(
            Long cursor,
            int size
    );

}

