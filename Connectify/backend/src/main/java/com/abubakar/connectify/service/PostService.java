package com.abubakar.connectify.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.request.CreatePostRequest;
import com.abubakar.connectify.dto.response.PostResponse;

public interface PostService {

    // Create Post
    PostResponse createPost(
            CreatePostRequest request,
            List<MultipartFile> files
    );

    // Update Post
    PostResponse updatePost(
            Long postId,
            CreatePostRequest request,
            List<MultipartFile> files
    );

    // Feed
    List<PostResponse> getFeed(Long cursor,int size);

    // Delete Post
    void deletePost(Long postId);

}

