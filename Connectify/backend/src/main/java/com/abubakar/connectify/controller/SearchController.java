package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.UserSearchRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.HashtagResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.UserSearchResponse;
import com.abubakar.connectify.service.SearchService;

import com.abubakar.connectify.util.PaginationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private static final Logger logger =
            LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    // ================= SEARCH USERS =================
    @GetMapping("/users")
    public ResponseEntity<CursorPageResponse<UserSearchResponse>>
    searchUsers(
            UserSearchRequest request,
            @RequestParam(required = false)
            Long cursor,
            @RequestParam(
                    defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING
            )
            int size

    ) {

        logger.info(
                """
                Search users API called
                | keyword: {}
                | verified: {}
                | emailVerified: {}
                | isPrivate: {}
                | city: {}
                | gender: {}
                | minFollowers: {}
                | cursor: {}
                | size: {}
                """,
                request.getKeyword(),
                request.getVerified(),
                request.getEmailVerified(),
                request.getIsPrivate(),
                request.getCity(),
                request.getGender(),
                request.getMinFollowers(),
                cursor,
                size
        );

        return ResponseEntity.ok(
                searchService.searchUsers(
                        request,
                        cursor,
                        size
                )
        );
    }

    // ================= SEARCH HASHTAGS =================
    @GetMapping("/hashtags")
    public ResponseEntity<CursorPageResponse<HashtagResponse>>
    searchHashtags(

            @RequestParam String keyword,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                """
                Search hashtags API called
                | keyword: {}
                | cursor: {}
                | size: {}
                """,
                keyword,
                cursor,
                size
        );

        return ResponseEntity.ok(
                searchService.searchHashtags(
                        keyword,
                        cursor,
                        size
                )
        );
    }

    // ================= POSTS BY HASHTAG =================
    @GetMapping("/hashtags/{hashtagName}/posts")
    public ResponseEntity<CursorPageResponse<PostResponse>>
    getPostsByHashtag(

            @PathVariable
            String hashtagName,

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue =
                    PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                """
                Posts by hashtag API called
                | hashtagName: {}
                | cursor: {}
                | size: {}
                """,
                hashtagName,
                cursor,
                size
        );

        return ResponseEntity.ok(
                searchService.getPostsByHashtag(
                        hashtagName,
                        cursor,
                        size
                )
        );
    }

    // ================= TRENDING POSTS =================
    @GetMapping("/trending/posts")
    public ResponseEntity<CursorPageResponse<PostResponse>>
    getTrendingPosts(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                """
                Trending posts API called
                | cursor: {}
                | size: {}
                """,
                cursor,
                size
        );

        return ResponseEntity.ok(
                searchService.getTrendingPosts(
                        cursor,
                        size
                )
        );
    }

    // ================= SUGGESTED USERS =================
    @GetMapping("/suggested/users")
    public ResponseEntity<CursorPageResponse<UserSearchResponse>>
    getSuggestedUsers(

            @RequestParam(required = false)
            Long cursor,

            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_STRING)
            int size
    ) {

        logger.info(
                """
                Suggested users API called
                | cursor: {}
                | size: {}
                """,
                cursor,
                size
        );

        return ResponseEntity.ok(
                searchService.getSuggestedUsers(
                        cursor,
                        size
                )
        );
    }

}

