package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.request.LoginRequest;
import com.abubakar.connectify.dto.request.RegisterRequest;
import com.abubakar.connectify.dto.request.ResetPasswordRequest;
import com.abubakar.connectify.dto.response.AuthResponse;
import com.abubakar.connectify.dto.response.UserResponse;

public interface AuthService {

    // ================= AUTH =================

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse createAdmin(RegisterRequest request);

    // ================= USER =================

    UserResponse getCurrentUser();

    UserResponse updateProfile(
            Long userId,
            UpdateProfileRequest request,
            MultipartFile file
    );

    // ================= PASSWORD =================

    void forgotPassword(String email);

    void resetPassword(
            String token,
            ResetPasswordRequest request
    );

    // ================= EMAIL VERIFICATION =================

    void sendEmailVerification();

    void verifyEmail(String token);

    // ================= ACCOUNT MANAGEMENT =================

    // User soft deletes own account
    void deactivateMyAccount();

    // User requests restore after self deactivation
    void requestAccountRestore();

    // User appeals admin ban
    void requestUnbanAppeal(
            String message
    );

}

