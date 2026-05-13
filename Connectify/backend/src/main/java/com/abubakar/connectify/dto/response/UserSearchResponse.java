package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSearchResponse {

    private Long id;

    private String name;

    private String uname;

    private String profileImageUrl;

    private Long followersCount;

    private Boolean following;

}

