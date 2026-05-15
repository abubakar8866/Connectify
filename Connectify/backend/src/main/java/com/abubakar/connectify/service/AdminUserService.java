package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.enums.AdminUserFilter;
import org.springframework.data.domain.Page;

public interface AdminUserService {

    Page<AdminUserResponse> getUsers(
            int page,
            int size,
            String keyword,
            AdminUserFilter filter
    );

    UserDetailsAdminResponse getUserDetails(
            Long userId
    );

    Page<AdminUserResponse> getReportedUsers(
            int page,
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

