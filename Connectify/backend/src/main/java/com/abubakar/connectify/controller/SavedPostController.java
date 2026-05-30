package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.SavePostResponse;
import com.abubakar.connectify.service.SavedPostService;

import com.abubakar.connectify.util.PaginationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/saved-posts")
public class SavedPostController {

    private static final Logger logger = LoggerFactory.getLogger(SavedPostController.class);

    @Autowired
    private SavedPostService savedPostService;

    // SAVE / UNSAVED
    @PostMapping("/{postId}/toggle")
    public ResponseEntity<SavePostResponse> toggleSavePost(
            @PathVariable Long postId
    ) {

        logger.info(
                "Toggle save post request api received | postId: {}",
                postId
        );

        SavePostResponse response =
                savedPostService.toggleSavePost(postId);

        return ResponseEntity.ok(response);
    }

    // GET SAVED POSTS
    @GetMapping
    public ResponseEntity<CursorPageResponse<PostResponse>>
    getSavedPosts(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                "Get saved posts request api received | cursor: {} | size: {}",
                cursor,
                size
        );

        CursorPageResponse<PostResponse> response =
                savedPostService.getSavedPosts(
                        cursor,
                        size
                );

        return ResponseEntity.ok(response);
    }

}

