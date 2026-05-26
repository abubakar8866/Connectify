package com.abubakar.connectify.dto.request;

import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;
import lombok.Data;

@Data
public class AdminUserSearchRequest {

    private String keyword;

    private Boolean verified;

    private Boolean emailVerified;

    private Boolean isPrivate;

    private Boolean active;

    private AccountStatus status;

    private String city;

    private Gender gender;

    private Long minFollowers;

    private Boolean restoreRequested;

    private Boolean unbanRequested;

    private Boolean deleted;

    private Boolean reportedOnly;

}

