package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminCommentResponse {

    private Long commentId;

    private String content;

    private Long userId;

    private String username;

    private Long postId;

    private Long likeCount;

    private Boolean deleted;

    private Long reportsCount;

    private LocalDateTime createdAt;

}