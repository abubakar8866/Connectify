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
@RequestMapping("/api/v1/comments")
public class CommentController {

    private static final Logger logger =
            LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentService commentService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {

        logger.info(
                "API request received | addComment | postId: {}",
                postId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        commentService.addComment(
                                postId,
                                request
                        )
                );
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CreateCommentRequest request
    ) {

        logger.info(
                "API request received | updateComment | commentId: {}",
                commentId
        );

        return ResponseEntity.ok(
                commentService.updateComment(
                        commentId,
                        request
                )
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId
    ) {

        logger.info(
                "API request received | deleteComment | commentId: {}",
                commentId
        );

        commentService.softDeleteComment(
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
                "API request received | requestRestoreComment | commentId: {}",
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
    public ResponseEntity<CursorPageResponse<CommentResponse>>
    getPostComments(

            @PathVariable Long postId,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                "API request received | getPostComments | postId: {} | cursor: {} | size: {}",
                postId,
                cursor,
                size
        );

        return ResponseEntity.ok(
                commentService.getPostComments(
                        postId,
                        cursor,
                        size
                )
        );
    }

}

