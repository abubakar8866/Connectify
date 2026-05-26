package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.CommentSearchRequest;
import com.abubakar.connectify.dto.response.AdminCommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.service.AdminCommentService;
import com.abubakar.connectify.util.PaginationConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminCommentController.class
            );

    @Autowired
    private AdminCommentService adminCommentService;

    // ================= GET COMMENTS =================
    @GetMapping
    public ResponseEntity<
            CursorPageResponse<AdminCommentResponse>
            > getComments(

            CommentSearchRequest request,

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
                API request received | getComments
                | keyword: {}
                | reportedOnly: {}
                | restoreRequested: {}
                | deleted: {}
                | cursor: {}
                | size: {}
                """,

                request.getKeyword(),

                request.getReportedOnly(),

                request.getRestoreRequested(),

                request.getDeleted(),

                cursor,

                size
        );

        return ResponseEntity.ok(

                adminCommentService.getAllComments(

                        request,

                        cursor,

                        size
                )
        );
    }

    // ================= MODERATE COMMENT =================
    @PatchMapping("/{commentId}/moderate")
    public ResponseEntity<String> moderateComment(
            @PathVariable Long commentId
    ) {

        logger.info(
                "API request received | moderateComment | commentId: {}",
                commentId
        );

        adminCommentService.moderateComment(
                commentId
        );

        return ResponseEntity.ok(
                "Comment moderated successfully"
        );
    }

    // ================= ACCEPT RESTORE COMMENT =================
    @PatchMapping("/{commentId}/restore/approve")
    public ResponseEntity<String> acceptRestoreComment(
            @PathVariable Long commentId
    ) {

        logger.info(
                "API request received | acceptRestoreComment | commentId: {}",
                commentId
        );

        adminCommentService.acceptRestoreComment(
                commentId
        );

        return ResponseEntity.ok(
                "Comment restore approved successfully"
        );
    }

    // ================= REJECT RESTORE COMMENT =================
    @PatchMapping("/{commentId}/restore/reject")
    public ResponseEntity<String> rejectRestoreComment(
            @PathVariable Long commentId
    ) {

        logger.info(
                "API request received | rejectRestoreComment | commentId: {}",
                commentId
        );

        adminCommentService.rejectRestoreComment(
                commentId
        );

        return ResponseEntity.ok(
                "Comment restore rejected successfully"
        );
    }

    // ================= HARD DELETE =================
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String>
    permanentlyDeleteComment(

            @PathVariable Long commentId
    ) {

        logger.info(
                "API request received | permanentlyDeleteComment | commentId: {}",
                commentId
        );

        adminCommentService
                .permanentlyDeleteComment(
                        commentId
                );

        return ResponseEntity.ok(
                "Comment permanently deleted"
        );
    }

}

