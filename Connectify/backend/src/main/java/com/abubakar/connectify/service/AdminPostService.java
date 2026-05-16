package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.response.AdminPostResponse;

import java.util.List;

public interface AdminPostService {

    List<AdminPostResponse> searchPosts(

            String keyword,
            String username,
            String hashtag,
            Boolean reportedOnly,
            Long cursor,
            int size
    );

}