package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.MediaType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoryResponse {

    private Long id;

    private String mediaUrl;

    private String thumbnailUrl;

    private MediaType mediaType;

    private String username;

    private String profileImageUrl;

    private Boolean isVerified;

    private Long viewCount;

    private Long reactionCount;

    private Long replyCount;

    private Boolean viewed;

    private Boolean reacted;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

}

