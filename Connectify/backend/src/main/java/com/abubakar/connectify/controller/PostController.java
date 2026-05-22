package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.CreatePostRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.service.PostService;

import com.abubakar.connectify.util.PaginationConstants;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;

    private static final Logger logger =
            LoggerFactory.getLogger(PostController.class);

   // CREATE POST
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "files",required = false)
            List<MultipartFile> files
    ) {

        logger.info(
                "Create post API initiated"
        );

        PostResponse response =
                postService.createPost(request, files);

        return ResponseEntity.ok(response);
    }

    // UPDATE POST
    @PutMapping(value = "/{postId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "files",required = false)
            List<MultipartFile> files
    ) {

        logger.info(
                "Update post API called | postId: {}",
                postId
        );

        PostResponse response =postService.updatePost(postId,request,files);

        return ResponseEntity.ok(response);
    }

    // GET SINGLE POST
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getSinglePost(
            @PathVariable Long postId
    ) {

        logger.info(
                "Get single post API called | postId: {}",
                postId
        );

        return ResponseEntity.ok(
                postService.getSinglePost(postId)
        );
    }

    // GET USER SPECIFIC POST
    @GetMapping("/user/{userId}")
    public ResponseEntity<
            CursorPageResponse<PostResponse>
            > getUserPosts(

            @PathVariable Long userId,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(
                    defaultValue =
                            PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size
    ) {

        logger.info(
                "Get user posts API called | userId: {} | cursor: {} | size: {}",
                userId,
                cursor,
                size
        );

        return ResponseEntity.ok(
                postService.getUserPosts(
                        userId,
                        cursor,
                        size
                )
        );
    }

    // GET FEED
    @GetMapping
    public ResponseEntity<
            CursorPageResponse<PostResponse>
            > getFeed(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                "Get feed API called | cursor: {} | size: {}",
                cursor,
                size
        );

        CursorPageResponse<PostResponse>
                response =
                postService.getFeed(
                        cursor,
                        size
                );

        logger.info(
                "Feed fetched successfully"
        );

        return ResponseEntity.ok(response);
    }

    // DELETE POST
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {

        logger.info(
                "Soft delete post API called | postId: {}",
                postId
        );

        postService.softDeletePost(postId);

        return ResponseEntity.ok("Post deleted successfully");
    }

    // RESTORE POST
    @PutMapping("/{postId}/restore-request")
    public ResponseEntity<String>
    requestRestorePost(
            @PathVariable Long postId
    ) {

        logger.info(
                "Restore post request API called | postId: {}",
                postId
        );

        postService.requestRestorePost(
                postId
        );

        return ResponseEntity.ok(
                "Restore request submitted successfully"
        );
    }

}

