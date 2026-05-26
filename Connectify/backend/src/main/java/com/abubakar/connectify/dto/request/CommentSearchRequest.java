package com.abubakar.connectify.dto.request;

import lombok.Data;

@Data
public class CommentSearchRequest {

    private String keyword;

    private Boolean reportedOnly;

    private Boolean restoreRequested;

    private Boolean deleted;

}

