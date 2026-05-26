package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

public interface AdminPostService {

    // ================= SEARCH POSTS =================
    CursorPageResponse<AdminPostResponse> searchPosts(

            String keyword,
            String username,
            String hashtag,
            Boolean reportedOnly,
            Boolean restoreRequested,
            Boolean deleted,
            Long cursor,
            int size
    );

    // ================= PERMANENT DELETE POST =================
    // Admin hard delete
    void permanentlyDeletePost(
            Long postId
    );

    // Admin restore post
    void restorePost(
            Long postId
    );

    //Admin reject restore post
    void rejectRestoreRequest(Long postId);

}

