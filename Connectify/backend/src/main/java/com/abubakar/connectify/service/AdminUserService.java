package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;

public interface AdminUserService {

    CursorPageResponse<AdminUserResponse> getUsers(
            Long cursor,
            int size,
            String keyword,
            Boolean verified,
            Boolean emailVerified,
            Boolean isPrivate,
            Boolean active,
            AccountStatus status,
            String city,
            Gender gender,
            Long minFollowers,
            Boolean restoreRequested,
            Boolean unbanRequested
    );

    UserDetailsAdminResponse getUserDetails(
            Long userId
    );

    CursorPageResponse<AdminUserResponse> getReportedUsers(
            Long cursor,
            int size
    );

    // ================= MODERATION =================

    void banUser(
            Long userId,
            BanUserRequest request
    );

    void unbanUser(
            Long userId
    );

    // Restore self-deactivated account
    void restoreUser(
            Long userId
    );

    // ================= REJECT RESTORE REQUEST =================
    void rejectRestoreRequest(
            Long userId
    );

    // Approve ban appeal
    void approveUnbanRequest(
            Long userId
    );

    // Reject ban appeal
    void rejectUnbanRequest(
            Long userId
    );

    // Permanent delete
    void deleteUser(
            Long userId
    );


}

