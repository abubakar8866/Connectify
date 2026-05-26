package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.MediaType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminStoryResponse {

    private Long id;

    private String username;

    private String profileImageUrl;

    private String mediaUrl;

    private MediaType mediaType;

    private Boolean deleted;

    private Boolean isActive;

    private Boolean restoreRequested;

    private Long reportCount;

    private Long viewCount;

    private Long reactionCount;

    private Long replyCount;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

}

