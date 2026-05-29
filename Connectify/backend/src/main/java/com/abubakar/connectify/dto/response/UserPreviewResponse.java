package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreviewResponse {

    private Long id;

    private String uname;

    private String profileImageUrl;

    private Boolean isVerified;

    private Boolean following;

}

