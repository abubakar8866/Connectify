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

    // ================= RESTORE COMMENT =================
    @PatchMapping("/{commentId}/restore")
    public ResponseEntity<String> restoreComment(
            @PathVariable Long commentId
    ) {

        logger.info(
                "API request received | restoreComment | commentId: {}",
                commentId
        );

        adminCommentService.restoreComment(
                commentId
        );

        return ResponseEntity.ok(
                "Comment restored successfully"
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

