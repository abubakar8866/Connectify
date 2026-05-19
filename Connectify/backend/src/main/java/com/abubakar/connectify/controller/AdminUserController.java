package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.service.AdminUserService;
import com.abubakar.connectify.util.PaginationConstants;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<CursorPageResponse<AdminUserResponse>>
    getUsers(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Boolean verified,

            @RequestParam(required = false)
            Boolean emailVerified,

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
            Long minFollowers,

            @RequestParam(required = false)
            Boolean restoreRequested,

            @RequestParam(required = false)
            Boolean unbanRequested,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size

    ) {

        return ResponseEntity.ok(

                adminUserService.getUsers(
                        cursor,
                        size,
                        keyword,
                        verified,
                        emailVerified,
                        isPrivate,
                        active,
                        status,
                        city,
                        gender,
                        minFollowers,
                        restoreRequested,
                        unbanRequested
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
    public ResponseEntity<CursorPageResponse<AdminUserResponse>>
    getReportedUsers(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        return ResponseEntity.ok(

                adminUserService.getReportedUsers(
                        cursor,
                        size
                )
        );
    }

    @PutMapping("/{userId}/restore")
    public ResponseEntity<String> restoreUser(
            @PathVariable Long userId
    ) {

        logger.info(
                "Restore user request received | userId: {}",
                userId
        );

        adminUserService.restoreUser(userId);

        return ResponseEntity.ok(
                "User restored successfully"
        );
    }

    @PutMapping("/{userId}/unban/approve")
    public ResponseEntity<String> approveUnbanRequest(
            @PathVariable Long userId
    ) {

        logger.info(
                "Approve unban request | userId: {}",
                userId
        );

        adminUserService.approveUnbanRequest(userId);

        return ResponseEntity.ok(
                "Unban request approved successfully"
        );
    }

    @PutMapping("/{userId}/unban/reject")
    public ResponseEntity<String> rejectUnbanRequest(
            @PathVariable Long userId
    ) {

        logger.info(
                "Reject unban request | userId: {}",
                userId
        );

        adminUserService.rejectUnbanRequest(userId);

        return ResponseEntity.ok(
                "Unban request rejected successfully"
        );
    }

}

