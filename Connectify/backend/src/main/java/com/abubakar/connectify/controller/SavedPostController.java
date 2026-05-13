package com.abubakar.connectify.controller;

import java.util.List;

import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.SavePostResponse;
import com.abubakar.connectify.service.SavedPostService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/saved-posts")
public class SavedPostController {

    private static final Logger logger = LoggerFactory.getLogger(SavedPostController.class);

    @Autowired
    private SavedPostService savedPostService;

    // SAVE / UNSAVED
    @PostMapping("/{postId}/toggle")
    public ResponseEntity<SavePostResponse> toggleSavePost(@PathVariable Long postId) {

        logger.info(
                "Toggle save post request received | postId: {}",
                postId
        );

        SavePostResponse response =
                savedPostService.toggleSavePost(postId);

        logger.info(
                "Toggle save completed | postId: {} | saved: {}",
                postId,
                response.getSaved()
        );

        return ResponseEntity.ok(response);
    }

    // GET SAVED POSTS
    @GetMapping
    public ResponseEntity<List<PostResponse>> getSavedPosts() {

        logger.info("Get saved posts request received");

        List<PostResponse> response =
                savedPostService.getSavedPosts();

        logger.info(
                "Saved posts fetched successfully | totalSavedPosts: {}",
                response.size()
        );

        return ResponseEntity.ok(response);
    }

}

