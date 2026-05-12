package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.request.LoginRequest;
import com.abubakar.connectify.dto.request.RegisterRequest;
import com.abubakar.connectify.dto.request.ResetPasswordRequest;
import com.abubakar.connectify.dto.response.AuthResponse;
import com.abubakar.connectify.dto.response.UserResponse;

public interface AuthService {

    // Register new user
    AuthResponse register(RegisterRequest request);

    // Login user
    AuthResponse login(LoginRequest request);

    // Get currently logged-in user
    UserResponse getCurrentUser();

    // Update user profile
    UserResponse updateProfile(Long userId,UpdateProfileRequest request,MultipartFile file);

    // Generate forgot password reset token
    void forgotPassword(String email);

    // Reset password using token
    void resetPassword(String token, ResetPasswordRequest request);

    // For registering admin
    AuthResponse createAdmin(RegisterRequest request);

}
