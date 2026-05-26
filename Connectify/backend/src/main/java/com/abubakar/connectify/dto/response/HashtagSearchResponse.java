package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HashtagSearchResponse {

    private Long id;

    private String name;

    private Long postCount;

    private CursorPageResponse<PostResponse> posts;

}

