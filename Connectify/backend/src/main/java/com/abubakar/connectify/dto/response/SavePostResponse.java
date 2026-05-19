package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavePostResponse {

    private Boolean saved;

    private Long postId;
}

