package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HashtagResponse {

    private Long id;

    private String name;

    private Long postCount;

}

