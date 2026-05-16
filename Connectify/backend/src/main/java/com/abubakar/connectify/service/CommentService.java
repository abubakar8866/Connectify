package com.abubakar.connectify.service;

import java.util.List;

import com.abubakar.connectify.dto.request.CreateCommentRequest;
import com.abubakar.connectify.dto.response.CommentResponse;

public interface CommentService {

    CommentResponse addComment(Long postId, CreateCommentRequest request);

    CommentResponse updateComment(Long commentId, CreateCommentRequest request);

    void deleteComment(Long commentId);

    List<CommentResponse> getPostComments(
            Long postId,
            Long cursor,
            int size
    );

}

