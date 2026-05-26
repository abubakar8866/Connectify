package com.abubakar.connectify.dto.request;

import lombok.Data;

@Data
public class PostSearchRequest {

    private String keyword;

    private String username;

    private String hashtag;

    private Boolean reportedOnly;

    private Boolean restoreRequested;

    private Boolean deleted;

}

