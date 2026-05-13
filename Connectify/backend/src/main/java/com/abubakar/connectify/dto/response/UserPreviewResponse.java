package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreviewResponse {

    private Long id;

    private String username;

    private String profileImage;

    private Boolean following;

}

