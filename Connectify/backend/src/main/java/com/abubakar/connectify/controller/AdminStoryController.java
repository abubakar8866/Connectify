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

    // ================= GET STORIES =================
    @GetMapping
    public ResponseEntity<
            CursorPageResponse<AdminStoryResponse>
            > getStories(

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
                """
                Admin get stories API called
                | username: {}
                | deleted: {}
                | isActive: {}
                | restoreRequested: {}
                | expired: {}
                | mediaType: {}
                | reportedOnly: {}
                | createdDate: {}
                | cursor: {}
                | size: {}
                """,
                request.getUsername(),
                request.getDeleted(),
                request.getIsActive(),
                request.getRestoreRequested(),
                request.getExpired(),
                request.getMediaType(),
                request.getReportedOnly(),
                request.getCreatedDate(),
                cursor,
                size
        );

        return ResponseEntity.ok(

                adminStoryService.getStories(
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= GET STORY BY ID =================
    @GetMapping("/{storyId}")
    public ResponseEntity<AdminStoryResponse>
    getStoryById(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin get story by id API called | storyId: {}",
                storyId
        );

        return ResponseEntity.ok(

                adminStoryService.getStoryById(
                        storyId
                )
        );
    }

    // ================= MODERATE STORY =================
    @PatchMapping("/{storyId}/moderate")
    public ResponseEntity<String>
    moderateStory(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin moderate story API called | storyId: {}",
                storyId
        );

        adminStoryService.moderateStory(
                storyId
        );

        return ResponseEntity.ok(
                "Story moderated successfully"
        );
    }

    // ================= APPROVE STORY RESTORE =================
    @PatchMapping("/{storyId}/restore/approve")
    public ResponseEntity<String>
    approveStoryRestore(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin approve story restore API called | storyId: {}",
                storyId
        );

        adminStoryService.approveStoryRestore(
                storyId
        );

        return ResponseEntity.ok(
                "Story restore approved successfully"
        );
    }

    // ================= REJECT STORY RESTORE =================
    @PatchMapping("/{storyId}/restore/reject")
    public ResponseEntity<String>
    rejectStoryRestore(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin reject story restore API called | storyId: {}",
                storyId
        );

        adminStoryService.rejectStoryRestore(
                storyId
        );

        return ResponseEntity.ok(
                "Story restore rejected successfully"
        );
    }

    // ================= EXPIRE STORY =================
    @PatchMapping("/{storyId}/expire")
    public ResponseEntity<String>
    expireStory(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin expire story API called | storyId: {}",
                storyId
        );

        adminStoryService.expireStory(
                storyId
        );

        return ResponseEntity.ok(
                "Story expired successfully"
        );
    }

    // ================= HARD DELETE STORY =================
    @DeleteMapping("/{storyId}/permanent")
    public ResponseEntity<String>
    permanentlyDeleteStory(
            @PathVariable Long storyId
    ) {

        logger.info(
                "Admin permanently delete story API called | storyId: {}",
                storyId
        );

        adminStoryService.permanentlyDeleteStory(
                storyId
        );

        return ResponseEntity.ok(
                "Story permanently deleted successfully"
        );
    }

}

