package com.abubakar.connectify.controller;

import java.util.Map;

import com.abubakar.connectify.dto.request.*;
import com.abubakar.connectify.util.JsonRequestParser;
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

    @Autowired
    private JsonRequestParser jsonRequestParser;

    private static final Logger logger =
            LoggerFactory.getLogger(AuthController.class);

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        logger.info(
                "Register Api initiated for : {}",
                request.getEmail()
        );

        AuthResponse response =
                authService.register(request);

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
                "Login Api initiated for: {}",
                request.getEmail()
        );

        AuthResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }

    // ================= REFRESH TOKEN =================
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody
            RefreshTokenRequest request
    ) {

        logger.info(
                "Refresh token API initiated"
        );

        AuthResponse response =
                authService.refreshToken(
                        request.getRefreshToken()
                );

        return ResponseEntity.ok(
                response
        );
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        logger.info(
                "Logout API initiated"
        );

        authService.logout();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logout successful"
                )
        );
    }

    // ================= CURRENT USER =================
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {

        logger.info("Fetching Current User Api initiated.");

        UserResponse response =
                authService.getCurrentUser();

        return ResponseEntity.ok(response);
    }

    // ================= UPDATE User =================
    @PutMapping(
            value = "/update-profile/{userId}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestPart("data") String data,
            @RequestPart(value = "file", required = false)
            MultipartFile file
    ){

        logger.info("Profile Api initiated for userId: {}",userId);

        UpdateProfileRequest request =
                jsonRequestParser.parseAndValidate(
                        data,
                        UpdateProfileRequest.class
                );

        UserResponse response =
                authService.updateProfile(
                        userId,
                        request,
                        file
                );

        return ResponseEntity.ok(response);

    }

    // ================= FORGOT PASSWORD =================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        logger.info(
                "Forgot password api initiated for: {}",
                request.getEmail()
        );

        authService.forgotPassword(request);

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

        logger.info("Reset password Api initiated");

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

    // ================= SEND EMAIL VERIFICATION =================
    @PostMapping("/email/send-verification")
    public ResponseEntity<String> sendEmailVerification() {

        logger.info(
                "Send verification email api initiated"
        );

        authService.sendEmailVerification();

        return ResponseEntity.ok(
                        "Verification email sent successfully"
        );
    }

    // ================= VERIFY EMAIL =================
    @GetMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token
    ) {

        logger.info(
                "Verify email api initiated"
        );

        authService.verifyEmail(token);

        return ResponseEntity.ok(
                        "Email verified successfully"
        );
    }

    // ================= DEACTIVATE ACCOUNT =================
    @PutMapping("/deactivate")
    public ResponseEntity<?> deactivateMyAccount() {

        logger.info(
                "Deactivate account api initiated"
        );

        authService.deactivateMyAccount();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Account deactivated successfully"
                )
        );

    }

    // ================= REQUEST ACCOUNT RESTORE =================
    @PostMapping("/restore-request")
    public ResponseEntity<?> requestAccountRestore() {

        logger.info(
                "Account restore api initiated"
        );

        authService.requestAccountRestore();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Restore request submitted successfully"
                )
        );

    }

    // ================= REQUEST UNBAN APPEAL =================
    @PostMapping("/unban-request")
    public ResponseEntity<?> requestUnbanAppeal(
            @RequestBody Map<String, String> request
    ) {

        logger.info(
                "Unban appeal api initiated"
        );

        authService.requestUnbanAppeal(
                request.get("message")
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Unban appeal submitted successfully"
                )
        );

    }

}

