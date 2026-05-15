package com.abubakar.connectify.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.AuthProvider;

import com.abubakar.connectify.enums.Gender;
import com.abubakar.connectify.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String name;

    private String uname;

    private String email;

    private String bio;

    private String profileImageUrl;

    private Role role;

    private Gender gender;

    private List<String> languages;

    private LocalDate dateOfBirth;

    private Integer age;

    private String city;

    private AccountStatus accountStatus;

    private AuthProvider provider;

    private Boolean isActive;

    private Boolean isEmailVerified;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

}

