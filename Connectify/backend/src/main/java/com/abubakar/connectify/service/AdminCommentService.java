package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.AdminCommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

import java.util.List;

public interface AdminCommentService {

    CursorPageResponse<AdminCommentResponse> getAllComments(
            Long cursor,
            int size,
            String keyword,
            Boolean reportedOnly
    );

    void deleteComment(
            Long commentId
    );

}

