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

    // MODERATION
    void moderateComment(
            Long commentId
    );

    // RESTORE APPROVAL
    void acceptRestoreComment(
            Long commentId
    );

    void rejectRestoreComment(
            Long commentId
    );

    // HARD DELETE
    void permanentlyDeleteComment(
            Long commentId
    );

}

