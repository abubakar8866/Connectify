package com.abubakar.connectify.controller;

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
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    @Autowired
    private AdminPostService adminPostService;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminPostController.class
            );

    @GetMapping
    public ResponseEntity<CursorPageResponse<AdminPostResponse>>
    searchPosts(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String username,

            @RequestParam(required = false)
            String hashtag,

            @RequestParam(required = false)
            Boolean reportedOnly,

            @RequestParam(required = false)
            Boolean restoreRequested,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size

    ) {

        logger.info(
                "Admin post search request"
        );

        return ResponseEntity.ok(
                adminPostService.searchPosts(
                        keyword,
                        username,
                        hashtag,
                        reportedOnly,
                        restoreRequested,
                        cursor,
                        size
                )
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String>
    permanentlyDeletePost(
            @PathVariable Long postId
    ) {

        logger.info(
                "Admin permanent delete API called | postId: {}",
                postId
        );

        adminPostService.permanentlyDeletePost(
                postId
        );

        return ResponseEntity.ok(
                "Post permanently deleted successfully"
        );
    }

    @PutMapping("/{postId}/restore")
    public ResponseEntity<String>
    restorePost(
            @PathVariable Long postId
    ) {

        logger.info(
                "Admin restore post API called"
        );

        adminPostService.restorePost(
                postId
        );

        return ResponseEntity.ok(
                "Post restored successfully"
        );
    }

}

