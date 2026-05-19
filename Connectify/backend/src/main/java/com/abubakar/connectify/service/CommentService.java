package com.abubakar.connectify.service;

import java.util.List;

import com.abubakar.connectify.dto.request.CreateCommentRequest;
import com.abubakar.connectify.dto.response.CommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

public interface CommentService {

    CommentResponse addComment(Long postId, CreateCommentRequest request);

    CommentResponse updateComment(Long commentId, CreateCommentRequest request);

    void softDeleteComment(Long commentId);

    CursorPageResponse<CommentResponse> getPostComments(
            Long postId,
            Long cursor,
            int size
    );

    void requestRestoreComment(
            Long commentId
    );

}

