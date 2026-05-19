package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.CreateCommentRequest;
import com.abubakar.connectify.dto.response.CommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.service.CommentService;

import com.abubakar.connectify.util.PaginationConstants;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final Logger logger =
            LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentService commentService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request) {

        logger.info(
                "Add comment request received | postId: {}",
                postId
        );

        CommentResponse response =
                commentService.addComment(postId, request);

        logger.info(
                "Comment added successfully | commentId: {}",
                response.getId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CreateCommentRequest request) {

        logger.info(
                "Update comment request received | commentId: {}",
                commentId
        );

        CommentResponse response =
                commentService.updateComment(commentId, request);

        logger.info(
                "Comment updated successfully | commentId: {}",
                response.getId()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {

        logger.info(
                "Delete comment request received | commentId: {}",
                commentId
        );

        commentService.softDeleteComment(commentId);

        logger.info(
                "Comment deleted successfully | commentId: {}",
                commentId
        );

        return ResponseEntity.ok(
                "Comment deleted successfully"
        );
    }

    @PostMapping("/{commentId}/restore-request")
    public ResponseEntity<String> requestRestoreComment(
            @PathVariable Long commentId
    ) {

        logger.info(
                "Restore request API called | commentId: {}",
                commentId
        );

        commentService.requestRestoreComment(
                commentId
        );

        return ResponseEntity.ok(
                "Restore request submitted successfully"
        );
    }

    @GetMapping("/post/{postId}")
    public  ResponseEntity<CursorPageResponse<CommentResponse>>
    getPostComments(

            @PathVariable Long postId,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                "Get comments request | postId: {} | cursor: {} | size: {}",
                postId,
                cursor,
                size
        );

        CursorPageResponse<CommentResponse> response =
                commentService.getPostComments(
                        postId,
                        cursor,
                        size
                );

        logger.info(
                "Comments fetched successfully | count: {}",
                response.getCurrentPageData()
        );

        return ResponseEntity.ok(response);
    }

}

