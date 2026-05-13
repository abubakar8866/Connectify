package com.abubakar.connectify.controller;

import java.util.List;

import com.abubakar.connectify.dto.response.FollowResponse;
import com.abubakar.connectify.dto.response.UserPreviewResponse;
import com.abubakar.connectify.service.FollowService;

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
                "Toggle follow request received | targetUserId: {}",
                userId
        );

        FollowResponse response =
                followService.toggleFollow(userId);

        logger.info(
                "Toggle follow completed successfully | targetUserId: {} | following: {}",
                userId,
                response.getFollowing()
        );

        return ResponseEntity.ok(response);
    }

    // GET FOLLOWERS
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserPreviewResponse>> getFollowers(
            @PathVariable Long userId) {

        logger.info(
                "Get followers request received | userId: {}",
                userId
        );

        List<UserPreviewResponse> response =
                followService.getFollowers(userId);

        logger.info(
                "Followers fetched successfully | userId: {} | totalFollowers: {}",
                userId,
                response.size()
        );

        return ResponseEntity.ok(response);
    }


    // GET FOLLOWING
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserPreviewResponse>> getFollowing(
            @PathVariable Long userId) {

        logger.info(
                "Get following request received | userId: {}",
                userId
        );

        List<UserPreviewResponse> response =
                followService.getFollowing(userId);

        logger.info(
                "Following fetched successfully | userId: {} | totalFollowing: {}",
                userId,
                response.size()
        );

        return ResponseEntity.ok(response);
    }

}

