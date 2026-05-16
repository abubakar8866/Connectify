package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.AdminUserFilter;
import com.abubakar.connectify.enums.Gender;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.service.AdminUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    private static final Logger logger =
            LoggerFactory.getLogger(AdminUserController.class);

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>>
    getUsers(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Boolean verified,

            @RequestParam(required = false)
            Boolean isPrivate,

            @RequestParam(required = false)
            Boolean active,

            @RequestParam(required = false)
            AccountStatus status,

            @RequestParam(required = false)
            String city,

            @RequestParam(required = false)
            Gender gender,

            @RequestParam(required = false)
            Long minFollowers
    ) {

        return ResponseEntity.ok(

                adminUserService.getUsers(

                        cursor,

                        size,

                        keyword,

                        verified,

                        isPrivate,

                        active,

                        status,

                        city,

                        gender,

                        minFollowers
                )
        );
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

           @Valid @RequestBody BanUserRequest request
    ) {

        logger.info(
                "Ban user request received | userId: {}",
                userId
        );

        if (Boolean.TRUE.equals(request.getPermanent())
                && request.getDurationInDays() != null) {

            throw new OperationFailException(
                    "Permanent ban should not have duration"
            );
        }

        if (!Boolean.TRUE.equals(request.getPermanent())
                && request.getDurationInDays() == null) {

            throw new OperationFailException(
                    "Duration is required for temporary ban"
            );
        }

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

    @GetMapping("/reported")
    public ResponseEntity<List<AdminUserResponse>>
    getReportedUsers(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(

                adminUserService.getReportedUsers(
                        cursor,
                        size
                )
        );
    }

}

