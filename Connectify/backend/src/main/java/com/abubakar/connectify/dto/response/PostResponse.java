package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PostResponse {

    private Long id;

    private String caption;

    private Long userId;

    private String username;

    private String userProfileImage;

    private Boolean isVerified;

    private Long likeCount;

    private Long commentCount;

    private Boolean liked;

    private Boolean mine;

    private LocalDateTime createdAt;

    private LocalDateTime savedAt;

    private LocalDateTime updatedAt;

    private List<String> mediaUrls;

    private List<String> hashtags;

}

