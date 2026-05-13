package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.CreatePostRequest;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.service.PostService;

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

        logger.info("Create post API called");

        PostResponse response =
                postService.createPost(request, files);

        logger.info(
                "Post created successfully with id: {}",
                response.getId()
        );

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

        logger.info("Update post API called for postId: {}",postId);

        PostResponse response =postService.updatePost(postId,request,files);

        logger.info("Post updated successfully with id: {}",postId);

        return ResponseEntity.ok(response);
    }

    // GET FEED
    @GetMapping
    public ResponseEntity<List<PostResponse>> getFeed(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {

        logger.info("Get feed API called | cursor: {} | size: {}",cursor,size);

        List<PostResponse> response = postService.getFeed(cursor, size);

        logger.info("Feed fetched successfully | total posts: {}",response.size());

        return ResponseEntity.ok(response);
    }

    // DELETE POST
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {

        logger.info("Delete post API called for postId: {}",postId);

        postService.deletePost(postId);

        logger.info("Post deleted successfully with id: {}",postId);

        return ResponseEntity.ok("Post deleted successfully");
    }

}