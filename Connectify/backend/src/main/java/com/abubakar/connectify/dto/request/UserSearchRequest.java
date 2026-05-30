package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.Gender;
import lombok.Data;

@Data
public class UserSearchRequest {

    private String keyword;

    private Boolean verified;

    private Boolean emailVerified;

    private Boolean isPrivate;

    private String city;

    private Gender gender;

    private Long minFollowers;

}

