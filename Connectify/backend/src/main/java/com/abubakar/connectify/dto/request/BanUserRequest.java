package com.abubakar.connectify.dto.request;

import lombok.Data;

@Data
public class BanUserRequest {

    private String reason;

    private Integer durationInDays;

    private String adminNote;

    private Boolean permanent;

}

