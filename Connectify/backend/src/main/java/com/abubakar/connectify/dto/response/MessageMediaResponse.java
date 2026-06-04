package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageMediaResponse {

    private Long id;

    private String mediaUrl;

    private MessageType mediaType;

    private Integer orderIndex;

}

