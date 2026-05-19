package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {

    private Long id;

    private String name;

    private String uname;

    private String email;

    private Boolean isActive;

    private Boolean isPrivate;

    private Boolean isVerified;

    private Boolean isEmailVerified;

    private AccountStatus accountStatus;

    private Long followersCount;

    private Long followingCount;

    private LocalDateTime createdAt;

}

