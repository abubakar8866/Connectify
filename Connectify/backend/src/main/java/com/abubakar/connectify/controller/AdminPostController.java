package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.PostSearchRequest;
import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.service.AdminPostService;
import com.abubakar.connectify.util.PaginationConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/posts")
public class AdminPostController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminPostController.class
            );

    @Autowired
    private AdminPostService adminPostService;

    // ================= GET POSTS =================
    @GetMapping
    public ResponseEntity<
            CursorPageResponse<AdminPostResponse>
            > getPosts(

            PostSearchRequest request,

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
                Admin get posts API called
                | keyword: {}
                | username: {}
                | hashtag: {}
                | reportedOnly: {}
                | restoreRequested: {}
                | deleted: {}
                | cursor: {}
                | size: {}
                """,
                request.getKeyword(),
                request.getUsername(),
                request.getHashtag(),
                request.getReportedOnly(),
                request.getRestoreRequested(),
                request.getDeleted(),
                cursor,
                size
        );

        return ResponseEntity.ok(

                adminPostService.getPosts(
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= MODERATE POST =================
    @PatchMapping("/{postId}/moderate")
    public ResponseEntity<String> moderatePost(
            @PathVariable Long postId
    ) {

        logger.info(
                "Admin moderate post API called | postId: {}",
                postId
        );

        adminPostService.moderatePost(
                postId
        );

        return ResponseEntity.ok(
                "Post moderated successfully"
        );
    }

    // ================= APPROVE RESTORE =================
    @PatchMapping("/{postId}/restore/approve")
    public ResponseEntity<String> approvePostRestore(
            @PathVariable Long postId
    ) {

        logger.info(
                "Admin approve post restore API called | postId: {}",
                postId
        );

        adminPostService.approvePostRestore(
                postId
        );

        return ResponseEntity.ok(
                "Post restore approved successfully"
        );
    }

    // ================= REJECT RESTORE =================
    @PatchMapping("/{postId}/restore/reject")
    public ResponseEntity<String> rejectPostRestore(
            @PathVariable Long postId
    ) {

        logger.info(
                "Admin reject post restore API called | postId: {}",
                postId
        );

        adminPostService.rejectPostRestore(
                postId
        );

        return ResponseEntity.ok(
                "Post restore rejected successfully"
        );
    }

    // ================= HARD DELETE =================
    @DeleteMapping("/{postId}/permanent")
    public ResponseEntity<String> permanentlyDeletePost(
            @PathVariable Long postId
    ) {

        logger.info(
                "Admin permanently delete post API called | postId: {}",
                postId
        );

        adminPostService.permanentlyDeletePost(
                postId
        );

        return ResponseEntity.ok(
                "Post permanently deleted successfully"
        );
    }

}

