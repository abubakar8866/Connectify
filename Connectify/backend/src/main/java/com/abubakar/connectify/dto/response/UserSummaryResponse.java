package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {

    private Long id;

    private String name;

    private String username;

    private String profileImageUrl;

    private Long followersCount;

    private Long followingCount;

    private Long postsCount;

}

