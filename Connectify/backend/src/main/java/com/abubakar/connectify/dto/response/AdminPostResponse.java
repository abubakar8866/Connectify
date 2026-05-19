package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminPostResponse {

    private Long postId;

    private String caption;

    private Long likeCount;

    private Long commentCount;

    private Boolean deleted;

    private Boolean restoreRequested;

    private Long reportCount;

    private String username;

    private List<String> hashtags;

    private List<String> mediaUrls;

    private LocalDateTime createdAt;

}

