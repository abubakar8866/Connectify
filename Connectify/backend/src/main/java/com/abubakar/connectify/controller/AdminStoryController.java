package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.AdminStoryFilterRequest;
import com.abubakar.connectify.dto.response.AdminStoryResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.service.AdminStoryService;
import com.abubakar.connectify.util.PaginationConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stories")
public class AdminStoryController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminStoryController.class
            );

    @Autowired
    private AdminStoryService adminStoryService;

    // ================= GET ALL Stories =================
    @GetMapping
    public ResponseEntity<
            CursorPageResponse<AdminStoryResponse>
            > getAllStories(

            AdminStoryFilterRequest request,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                "Admin get all stories api request received | cursor: {} | size: {}",
                cursor,
                size
        );

        return ResponseEntity.ok(
                adminStoryService.getAllStories(
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= GET SINGLE STORY =================
    @GetMapping("/{storyId}")
    public ResponseEntity<AdminStoryResponse>
    getStoryById(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin get story by id api request received | storyId: {}",
                storyId
        );

        return ResponseEntity.ok(
                adminStoryService.getStoryById(storyId)
        );
    }

    // ================= DELETE STORY =================
    @DeleteMapping("/{storyId}")
    public ResponseEntity<String>
    deleteStory(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin delete story api request received | storyId: {}",
                storyId
        );

        adminStoryService.deleteStory(storyId);

        return ResponseEntity.ok(
                "Story deleted successfully"
        );
    }

    // ================= RESTORE STORY =================
    @PutMapping("/{storyId}/restore")
    public ResponseEntity<String>
    restoreStory(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin restore story request received | storyId: {}",
                storyId
        );

        adminStoryService.restoreStory(storyId);

        return ResponseEntity.ok(
                "Story restored successfully"
        );
    }

    // ================= GET RESTORE REQUEST =================
    @GetMapping("/restore-requests")
    public ResponseEntity<
            CursorPageResponse<AdminStoryResponse>
            > getRestoreRequests(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                "Admin get restore requests api request received | cursor: {} | size: {}",
                cursor,
                size
        );

        return ResponseEntity.ok(
                adminStoryService.getRestoreRequests(
                        cursor,
                        size
                )
        );
    }

    // ================= APPROVE RESTORE REQUEST =================
    @PutMapping("/{storyId}/approve-restore")
    public ResponseEntity<String>
    approveRestoreRequest(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin approve restore request api received | storyId: {}",
                storyId
        );

        adminStoryService.approveRestoreRequest(storyId);

        return ResponseEntity.ok(
                "Restore request approved successfully"
        );
    }

    // ================= REJECT RESTORE REQUEST =================
    @PutMapping("/{storyId}/reject-restore")
    public ResponseEntity<String>
    rejectRestoreRequest(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin reject restore request api received | storyId: {}",
                storyId
        );

        adminStoryService.rejectRestoreRequest(storyId);

        return ResponseEntity.ok(
                "Restore request rejected successfully"
        );
    }

    // ================= EXPIRE STORY =================
    @PutMapping("/{storyId}/expire")
    public ResponseEntity<String>
    expireStory(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin expire story api request received | storyId: {}",
                storyId
        );

        adminStoryService.expireStory(storyId);

        return ResponseEntity.ok(
                "Story expired successfully"
        );
    }

}

