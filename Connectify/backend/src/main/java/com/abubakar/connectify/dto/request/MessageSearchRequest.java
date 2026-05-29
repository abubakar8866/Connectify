package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.MessageType;
import lombok.Data;

@Data
public class MessageSearchRequest {

    private String keyword;

    private String username;

    private MessageType messageType;

    private Boolean deletedByAdmin;

    private Boolean restoreRequested;

    private Boolean reportedOnly;

}

