package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.PostSearchRequest;
import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

public interface AdminPostService {

    CursorPageResponse<AdminPostResponse> getPosts(
            PostSearchRequest request,
            Long cursor,
            int size
    );

    // MODERATION
    void moderatePost(
            Long postId
    );

    // RESTORE
    void approvePostRestore(
            Long postId
    );

    void rejectPostRestore(
            Long postId
    );

    // HARD DELETE
    void permanentlyDeletePost(
            Long postId
    );

}

