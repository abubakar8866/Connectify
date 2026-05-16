package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.AdminCommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.service.AdminCommentService;

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
    private AdminCommentService
            adminCommentService;

    @GetMapping
    public ResponseEntity<CursorPageResponse<AdminCommentResponse>>
    getComments(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Boolean reportedOnly
    ) {

        logger.info(
                "Get comments request received"
        );

        return ResponseEntity.ok(
                adminCommentService
                        .getAllComments(
                                cursor,
                                size,
                                keyword,
                                reportedOnly
                        )
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId
    ) {

        logger.info(
                "Delete abusive comment request received"
        );

        adminCommentService
                .deleteComment(commentId);

        return ResponseEntity.ok(
                "Comment deleted successfully"
        );
    }

}

