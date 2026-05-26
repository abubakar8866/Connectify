package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.CommentSearchRequest;
import com.abubakar.connectify.dto.response.AdminCommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;

import java.util.List;

public interface AdminCommentService {

    CursorPageResponse<AdminCommentResponse>
    getAllComments(

            CommentSearchRequest request,

            Long cursor,

            int size
    );

    void restoreComment(
            Long commentId
    );

    void permanentlyDeleteComment(
            Long commentId
    );

}

