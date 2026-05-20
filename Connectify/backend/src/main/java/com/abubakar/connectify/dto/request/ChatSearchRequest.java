package com.abubakar.connectify.dto.request;

import lombok.Data;

@Data
public class ChatSearchRequest {

    private String keyword;

    private Boolean deletedByAdmin;

}

