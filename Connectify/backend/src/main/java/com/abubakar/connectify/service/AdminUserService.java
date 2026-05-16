package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;

import java.util.List;

public interface AdminUserService {

    List<AdminUserResponse> getUsers(
            Long cursor,
            int size,
            String keyword,
            Boolean verified,
            Boolean isPrivate,
            Boolean active,
            AccountStatus status,
            String city,
            Gender gender,
            Long minFollowers
    );

    UserDetailsAdminResponse getUserDetails(
            Long userId
    );

    List<AdminUserResponse> getReportedUsers(

            Long cursor,

            int size
    );

    void banUser(
            Long userId,
            BanUserRequest request
    );

    void unbanUser(
            Long userId
    );

    void deleteUser(
            Long userId
    );

}