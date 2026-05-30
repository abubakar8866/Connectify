package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.LikeResponse;
import com.abubakar.connectify.service.LikeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    private static final Logger logger =
            LoggerFactory.getLogger(LikeController.class);

    @Autowired
    private LikeService likeService;

    @PostMapping("/posts/{postId}/toggle")
    public ResponseEntity<LikeResponse> togglePostLike(
            @PathVariable Long postId) {

        logger.info(
                "Toggle post like API called | postId: {}",
                postId
        );

        return ResponseEntity.ok(
                likeService.togglePostLike(postId)
        );
    }

    @PostMapping("/comments/{commentId}/toggle")
    public ResponseEntity<LikeResponse> toggleCommentLike(
            @PathVariable Long commentId) {

        logger.info(
                "Toggle comment like API called | commentId: {}",
                commentId
        );

        return ResponseEntity.ok(
                likeService.toggleCommentLike(commentId)
        );
    }

}

