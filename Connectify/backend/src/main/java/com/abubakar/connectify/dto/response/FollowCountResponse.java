package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowCountResponse {

    private Long userId;

    private Long followersCount;

    private Long followingCount;

}

