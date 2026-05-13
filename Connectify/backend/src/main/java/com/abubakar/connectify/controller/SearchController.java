package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.HashtagResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.UserSearchResponse;
import com.abubakar.connectify.service.SearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger =
            LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    // ================= SEARCH USERS =================
    @GetMapping("/users")
    public ResponseEntity<List<UserSearchResponse>>
    searchUsers(
            @RequestParam String keyword
    ) {

        logger.info(
                "Search users request | keyword: {}",
                keyword
        );

        List<UserSearchResponse> response =
                searchService.searchUsers(keyword);

        logger.info(
                "Users search completed | resultCount: {}",
                response.size()
        );

        return ResponseEntity.ok(response);
    }

    // ================= SEARCH HASHTAGS =================
    @GetMapping("/hashtags")
    public ResponseEntity<List<HashtagResponse>>
    searchHashtags(
            @RequestParam String keyword
    ) {

        logger.info(
                "Search hashtags request | keyword: {}",
                keyword
        );

        List<HashtagResponse> response =
                searchService.searchHashtags(keyword);

        logger.info(
                "Hashtag search completed | resultCount: {}",
                response.size()
        );

        return ResponseEntity.ok(response);
    }

    // ================= TRENDING POSTS =================
    @GetMapping("/trending/posts")
    public ResponseEntity<List<PostResponse>>
    getTrendingPosts() {

        logger.info(
                "Trending posts request received"
        );

        List<PostResponse> response =
                searchService.getTrendingPosts();

        logger.info(
                "Trending posts fetched | totalPosts: {}",
                response.size()
        );

        return ResponseEntity.ok(response);
    }

    // ================= SUGGESTED USERS =================
    @GetMapping("/suggested/users")
    public ResponseEntity<List<UserSearchResponse>>
    getSuggestedUsers() {

        logger.info(
                "Suggested users request received"
        );

        List<UserSearchResponse> response =
                searchService.getSuggestedUsers();

        logger.info(
                "Suggested users fetched | totalUsers: {}",
                response.size()
        );

        return ResponseEntity.ok(response);
    }

}

