package com.abubakar.connectify.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {

    private Long id;

    private String content;

    private Long userId;

    private String username;

    private String userProfileImage;

    private Long likeCount;

    private Boolean liked;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<CommentResponse> replies;

    private Long replyCount;

}

