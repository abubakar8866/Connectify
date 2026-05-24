package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorySummaryResponse {

    private Long id;

    private String username;

    private String mediaUrl;

    private MediaType mediaType;

    private Long viewCount;

    private Long reactionCount;

}

