package com.abubakar.connectify.service;

import java.util.List;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.request.CreatePostRequest;
import com.abubakar.connectify.dto.response.PostResponse;

public interface PostService {

    // ================= CREATE POST =================
    PostResponse createPost(
            CreatePostRequest request,
            List<MultipartFile> files
    );

    // ================= UPDATE POST =================
    PostResponse updatePost(
            Long postId,
            CreatePostRequest request,
            List<MultipartFile> files
    );

    // ================= GET FEED =================
    // Personalized feed (followed users + own posts)
    CursorPageResponse<PostResponse> getFeed(
            Long cursor,
            int size
    );

    // ================= GET SINGLE POST =================
    PostResponse getSinglePost(
            Long postId
    );

    // ================= GET USER POSTS =================
    CursorPageResponse<PostResponse> getUserPosts(
            Long userId,
            Long cursor,
            int size
    );

    // ================= SOFT DELETE POST =================
    void softDeletePost(
            Long postId
    );

    // ================= Restore POST =================
    void requestRestorePost(
            Long postId
    );

}

