package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.StoryReactionRequest;
import com.abubakar.connectify.dto.request.StoryReplyRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.StoryResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import com.abubakar.connectify.service.StoryService;

import com.abubakar.connectify.util.PaginationConstants;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private static final Logger logger =
            LoggerFactory.getLogger(StoryController.class);

    @Autowired
    private StoryService storyService;

    // ================= CREATE STORY =================
    @PostMapping
    public ResponseEntity<StoryResponse> createStory(

            @RequestParam("file")
            MultipartFile file
    ) {

        logger.info("Create story request received");

        StoryResponse response =
                storyService.createStory(file);

        logger.info(
                "Story created successfully | storyId: {}",
                response.getId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ================= GET ACTIVE STORIES =================
    @GetMapping
    public ResponseEntity<CursorPageResponse<StoryResponse>> getActiveStories(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info("Get active stories request received");

        CursorPageResponse<StoryResponse> responses =
                storyService.getActiveStories(
                        cursor,
                        size
                );

        logger.info(
                "Stories fetched successfully | totalStories: {}",
                responses.getCurrentPageData()
        );

        return ResponseEntity.ok(responses);
    }

    // ================= VIEW STORY =================
    @PostMapping("/{storyId}/view")
    public ResponseEntity<String> viewStory(
            @PathVariable Long storyId
    ) {

        logger.info(
                "View story request received | storyId: {}",
                storyId
        );

        storyService.viewStory(storyId);

        logger.info(
                "Story viewed successfully | storyId: {}",
                storyId
        );

        return ResponseEntity.ok(
                "Story viewed successfully"
        );
    }

    // ================= REACT STORY =================
    @PostMapping("/{storyId}/react")
    public ResponseEntity<String> reactStory(
            @PathVariable Long storyId,
            @Valid @RequestBody StoryReactionRequest request
    ) {

        logger.info(
                "React story request received | storyId: {}",
                storyId
        );

        storyService.reactStory(
                storyId,
                request
        );

        logger.info(
                "Story reaction processed successfully | storyId: {}",
                storyId
        );

        return ResponseEntity.ok(
                "Story reaction updated successfully"
        );
    }

    // ================= REPLY STORY =================
    @PostMapping("/{storyId}/reply")
    public ResponseEntity<String> replyStory(
            @PathVariable Long storyId,
            @Valid @RequestBody StoryReplyRequest request
    ) {

        logger.info(
                "Reply story request received | storyId: {}",
                storyId
        );

        storyService.replyStory(
                storyId,
                request
        );

        logger.info(
                "Story reply added successfully | storyId: {}",
                storyId
        );

        return ResponseEntity.ok(
                "Story reply sent successfully"
        );
    }

    // ================= DELETE STORY =================
    @DeleteMapping("/{storyId}")
    public ResponseEntity<String> deleteStory(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Delete story request received | storyId: {}",
                storyId
        );

        storyService.deleteStory(storyId);

        logger.info(
                "Story deleted successfully | storyId: {}",
                storyId
        );

        return ResponseEntity.ok(
                "Story deleted successfully"
        );
    }

    // ================= GET STORY VIEWERS =================
    @GetMapping("/{storyId}/viewers")
    public ResponseEntity<CursorPageResponse<UserResponse>> getStoryViewers(

            @PathVariable Long storyId,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                "Get story viewers request received | storyId: {}",
                storyId
        );

        CursorPageResponse<UserResponse> viewers =
                storyService.getStoryViewers(
                        storyId,
                        cursor,
                        size
                );

        logger.info(
                "Story viewers fetched successfully | totalViewers: {}",
                viewers.getCurrentPageData()
        );

        return ResponseEntity.ok(viewers);
    }

    // ================= GET OWN STORY =================
    @GetMapping("/me")
    public ResponseEntity<
            CursorPageResponse<StoryResponse>
            > getMyStories(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        return ResponseEntity.ok(
                storyService.getMyStories(
                        cursor,
                        size
                )
        );
    }

}

