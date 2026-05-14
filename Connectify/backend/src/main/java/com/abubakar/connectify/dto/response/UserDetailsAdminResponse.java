package com.abubakar.connectify.dto.response;

import com.abubakar.connectify.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDetailsAdminResponse {

    private Long id;

    private String name;

    private String uname;

    private String email;

    private String bio;

    private String profileImageUrl;

    private Boolean isActive;

    private Boolean isPrivate;

    private Boolean isVerified;

    private Long followersCount;

    private Long followingCount;

    private Long postsCount;

    private Long reportsCount;

    private AccountStatus accountStatus;

    private LocalDateTime createdAt;

}

