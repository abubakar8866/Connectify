package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.enums.AdminUserFilter;
import com.abubakar.connectify.service.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    private static final Logger logger =
            LoggerFactory.getLogger(AdminUserController.class);

    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getUsers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            AdminUserFilter filter
    ) {

        logger.info(
                "Get users request received"
        );

        Page<AdminUserResponse> response =
                adminUserService.getUsers(
                        page,
                        size,
                        keyword,
                        filter
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailsAdminResponse>
    getUserDetails(
            @PathVariable Long userId
    ) {

        logger.info(
                "Get user details request | userId: {}",
                userId
        );

        UserDetailsAdminResponse response =
                adminUserService.getUserDetails(
                        userId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/ban")
    public ResponseEntity<String> banUser(

            @PathVariable Long userId,

            @RequestBody BanUserRequest request
    ) {

        logger.info(
                "Ban user request received | userId: {}",
                userId
        );

        adminUserService.banUser(
                userId,
                request
        );

        return ResponseEntity.ok(
                "User banned successfully"
        );
    }

    @PutMapping("/{userId}/unban")
    public ResponseEntity<String> unbanUser(
            @PathVariable Long userId
    ) {

        logger.info(
                "Unban user request received | userId: {}",
                userId
        );

        adminUserService.unbanUser(userId);

        return ResponseEntity.ok(
                "User unbanned successfully"
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long userId
    ) {

        logger.info(
                "Delete user request received | userId: {}",
                userId
        );

        adminUserService.deleteUser(userId);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }

}