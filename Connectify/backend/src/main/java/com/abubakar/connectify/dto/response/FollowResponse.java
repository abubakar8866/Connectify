package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowResponse {

    private Long targetUserId;

    private Boolean following;

    private Long targetFollowersCount;

    private Long currentUserFollowingCount;

}

