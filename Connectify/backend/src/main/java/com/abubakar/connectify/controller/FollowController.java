package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.CursorCountResponse;
import com.abubakar.connectify.dto.response.FollowCountResponse;
import com.abubakar.connectify.dto.response.FollowResponse;
import com.abubakar.connectify.dto.response.UserPreviewResponse;
import com.abubakar.connectify.service.FollowService;

import com.abubakar.connectify.util.PaginationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private static final Logger logger =
            LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private FollowService followService;

    // TOGGLE FOLLOW
    @PostMapping("/{userId}/toggle")
    public ResponseEntity<FollowResponse> toggleFollow(
            @PathVariable Long userId) {

        logger.info(
                "Toggle follow API called | targetUserId: {}",
                userId
        );

        FollowResponse response =
                followService.toggleFollow(userId);

        return ResponseEntity.ok(response);
    }

    // GET FOLLOWERS
    @GetMapping("/{userId}/followers")
    public ResponseEntity<
            CursorCountResponse<UserPreviewResponse>
            > getFollowers(

            @PathVariable Long userId,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                """
                Get followers request
                | userId: {}
                | cursor: {}
                | size: {}
                """,
                userId,
                cursor,
                size
        );

        CursorCountResponse<UserPreviewResponse>
                response =
                followService.getFollowers(
                        userId,
                        cursor,
                        size
                );

        return ResponseEntity.ok(response);
    }

    // GET FOLLOWING
    @GetMapping("/{userId}/following")
    public ResponseEntity<
            CursorCountResponse<UserPreviewResponse>
            > getFollowing(

            @PathVariable Long userId,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                """
                Get following request
                | userId: {}
                | cursor: {}
                | size: {}
                """,
                userId,
                cursor,
                size
        );

        CursorCountResponse<UserPreviewResponse>
                response =
                followService.getFollowing(
                        userId,
                        cursor,
                        size
                );

        return ResponseEntity.ok(response);
    }

    // ================= FOLLOW COUNTS =================
    @GetMapping("/counts")
    public ResponseEntity<FollowCountResponse>
    getFollowCounts(
            @RequestParam(required = false)
            Long userId
    ) {

        logger.info(
                "Follow count API called | userId: {}",
                userId
        );

        FollowCountResponse response =
                followService.getFollowCounts(
                        userId
                );

        return ResponseEntity.ok(response);
    }

}

