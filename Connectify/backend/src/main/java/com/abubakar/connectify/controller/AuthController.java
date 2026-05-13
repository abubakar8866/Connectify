package com.abubakar.connectify.controller;

import java.util.Map;

import com.abubakar.connectify.dto.request.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.response.AuthResponse;
import com.abubakar.connectify.dto.response.UserResponse;

import com.abubakar.connectify.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    private static final Logger logger =
            LoggerFactory.getLogger(AuthController.class);

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        logger.info(
                "Register request received for: {}",
                request.getEmail()
        );

        AuthResponse response =
                authService.register(request);

        logger.info(
                "User registered successfully: {}",
                request.getEmail()
        );

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        logger.info(
                "Login request received for: {}",
                request.getEmail()
        );

        AuthResponse response =
                authService.login(request);

        logger.info(
                "Login successful for: {}",
                request.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    // ================= CURRENT USER =================
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {

        logger.info("Fetching current authenticated user");

        UserResponse response =
                authService.getCurrentUser();

        return ResponseEntity.ok(response);
    }

    // ================= UPDATE User =================
    @PutMapping(
            value = "/update-profile/{userId}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long userId,@Valid @RequestPart("data") UpdateProfileRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        logger.info("Combined profile update request for userId: {}",userId);

        UserResponse response =authService.updateProfile(userId,request,file);

        return ResponseEntity.ok(response);
    }

    // ================= FORGOT PASSWORD =================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        logger.info(
                "Forgot password request for: {}",
                request.getEmail()
        );

        authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password reset link sent successfully"
                )
        );
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        logger.info("Reset password request received");

        authService.resetPassword(
                token,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password reset successful"
                )
        );
    }

    // ================= CREATE ADMIN =================
    @PostMapping("/create-admin")
    public ResponseEntity<AuthResponse> createAdmin(
            @Valid @RequestBody RegisterRequest request
    ) {

        logger.info(
                "Admin creation API called for: {}",
                request.getEmail()
        );

        AuthResponse response =
                authService.createAdmin(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

}

