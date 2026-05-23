package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.MediaType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdminStoryFilterRequest {

    private String username;

    private Boolean deleted;

    private Boolean isActive;

    private Boolean restoreRequested;

    private Boolean expired;

    private MediaType mediaType;

    private LocalDate createdDate;

}

