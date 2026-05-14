package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.AdminCommentResponse;
import org.springframework.data.domain.Page;

public interface AdminCommentService {

    Page<AdminCommentResponse> getAllComments(
            int page,
            int size,
            String keyword
    );

    Page<AdminCommentResponse> getReportedComments(
            int page,
            int size
    );

    void deleteComment(
            Long commentId
    );

}