package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowResponse {

    private Boolean following;

    private Long followersCount;

    private Long followingCount;

}

