package com.abubakar.connectify.dto.response;

import java.time.LocalDateTime;

import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.AuthProvider;

import com.abubakar.connectify.enums.Role;
import lombok.Getter;

@Getter
public class UserResponse {

    private Long id;

    private String name;

    private String uname;

    private String email;

    private String bio;

    private String profileImageUrl;

    private Role role;

    private AccountStatus accountStatus;

    private AuthProvider provider;

    private Boolean isActive;

    private Boolean isEmailVerified;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

}
