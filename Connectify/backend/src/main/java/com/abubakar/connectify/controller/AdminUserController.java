package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.AdminUserSearchRequest;
import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
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

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminUserController.class
            );

    @Autowired
    private AdminUserService adminUserService;

    // ================= GET USERS =================
    @GetMapping
    public ResponseEntity<
            CursorPageResponse<AdminUserResponse>
            > getUsers(

            AdminUserSearchRequest request,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                """
                Admin get users API called
                | keyword: {}
                | status: {}
                | restoreRequested: {}
                | unbanRequested: {}
                | deleted: {}
                | reportedOnly: {}
                | cursor: {}
                | size: {}
                """,
                request.getKeyword(),
                request.getStatus(),
                request.getRestoreRequested(),
                request.getUnbanRequested(),
                request.getDeleted(),
                request.getReportedOnly(),
                cursor,
                size
        );

        return ResponseEntity.ok(

                adminUserService.getUsers(
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= GET USER DETAILS =================
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailsAdminResponse>
    getUserDetails(
            @PathVariable Long userId
    ) {

        logger.info(
                "Admin get user details API called | userId: {}",
                userId
        );

        return ResponseEntity.ok(
                adminUserService.getUserDetails(
                        userId
                )
        );
    }

    // ================= MODERATE USER =================
    @PatchMapping("/{userId}/moderate")
    public ResponseEntity<String> moderateUser(

            @PathVariable Long userId,

            @Valid
            @RequestBody
            BanUserRequest request
    ) {

        logger.info(
                "Admin moderate user API called | userId: {}",
                userId
        );

        adminUserService.moderateUser(
                userId,
                request
        );

        return ResponseEntity.ok(
                "User moderated successfully"
        );
    }

    // ================= APPROVE USER RESTORE =================
    @PatchMapping("/{userId}/restore/approve")
    public ResponseEntity<String> approveUserRestore(
            @PathVariable Long userId
    ) {

        logger.info(
                "Admin approve user restore API called | userId: {}",
                userId
        );

        adminUserService.approveUserRestore(
                userId
        );

        return ResponseEntity.ok(
                "User restore approved successfully"
        );
    }

    // ================= REJECT USER RESTORE =================
    @PatchMapping("/{userId}/restore/reject")
    public ResponseEntity<String> rejectUserRestore(
            @PathVariable Long userId
    ) {

        logger.info(
                "Admin reject user restore API called | userId: {}",
                userId
        );

        adminUserService.rejectUserRestore(
                userId
        );

        return ResponseEntity.ok(
                "User restore rejected successfully"
        );
    }

    // ================= APPROVE USER UNBAN =================
    @PatchMapping("/{userId}/unban/approve")
    public ResponseEntity<String> approveUserUnban(
            @PathVariable Long userId
    ) {

        logger.info(
                "Admin approve user unban API called | userId: {}",
                userId
        );

        adminUserService.approveUserUnban(
                userId
        );

        return ResponseEntity.ok(
                "User unban approved successfully"
        );
    }

    // ================= REJECT USER UNBAN =================
    @PatchMapping("/{userId}/unban/reject")
    public ResponseEntity<String> rejectUserUnban(
            @PathVariable Long userId
    ) {

        logger.info(
                "Admin reject user unban API called | userId: {}",
                userId
        );

        adminUserService.rejectUserUnban(
                userId
        );

        return ResponseEntity.ok(
                "User unban rejected successfully"
        );
    }

    // ================= HARD DELETE USER =================
    @DeleteMapping("/{userId}/permanent")
    public ResponseEntity<String> permanentlyDeleteUser(
            @PathVariable Long userId
    ) {

        logger.info(
                "Admin permanently delete user API called | userId: {}",
                userId
        );

        adminUserService.permanentlyDeleteUser(
                userId
        );

        return ResponseEntity.ok(
                "User permanently deleted successfully"
        );
    }

}

