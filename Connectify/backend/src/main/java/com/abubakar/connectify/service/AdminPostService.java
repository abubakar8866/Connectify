package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.AdminPostResponse;
import org.springframework.data.domain.Page;

public interface AdminPostService {

    Page<AdminPostResponse> getAllPosts(
            int page,
            int size
    );

    Page<AdminPostResponse> getReportedPosts(
            int page,
            int size
    );

    Page<AdminPostResponse> searchPostsByKeyword(
            String keyword,
            int page,
            int size
    );

    Page<AdminPostResponse> searchPostsByUsername(
            String username,
            int page,
            int size
    );

    Page<AdminPostResponse> searchPostsByHashtag(
            String hashtag,
            int page,
            int size
    );

}

