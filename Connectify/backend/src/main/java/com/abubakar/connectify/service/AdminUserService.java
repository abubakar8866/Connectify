package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.AdminUserSearchRequest;
import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;

public interface AdminUserService {

    CursorPageResponse<AdminUserResponse>
    getUsers(
            AdminUserSearchRequest request,
            Long cursor,
            int size
    );

    UserDetailsAdminResponse getUserDetails(
            Long userId
    );

    // MODERATION
    void moderateUser(
            Long userId,
            BanUserRequest request
    );

    // UNBAN
    void approveUserUnban(
            Long userId
    );

    void rejectUserUnban(
            Long userId
    );

    // RESTORE
    void approveUserRestore(
            Long userId
    );

    void rejectUserRestore(
            Long userId
    );

    // HARD DELETE
    void permanentlyDeleteUser(
            Long userId
    );

}

