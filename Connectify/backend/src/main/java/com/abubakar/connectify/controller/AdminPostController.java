package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.service.AdminPostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Page<AdminPostResponse>>
    getAllPosts(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        logger.info(
                "Get all posts request"
        );

        return ResponseEntity.ok(
                adminPostService.getAllPosts(
                        page,
                        size
                )
        );
    }

    @GetMapping("/reported")
    public ResponseEntity<Page<AdminPostResponse>>
    getReportedPosts(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        logger.info(
                "Get reported posts request"
        );

        return ResponseEntity.ok(
                adminPostService.getReportedPosts(
                        page,
                        size
                )
        );
    }

    @GetMapping("/search/keyword")
    public ResponseEntity<Page<AdminPostResponse>>
    searchByKeyword(

            @RequestParam String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(
                adminPostService.searchPostsByKeyword(
                        keyword,
                        page,
                        size
                )
        );
    }

    @GetMapping("/search/user")
    public ResponseEntity<Page<AdminPostResponse>>
    searchByUsername(

            @RequestParam String username,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(
                adminPostService.searchPostsByUsername(
                        username,
                        page,
                        size
                )
        );
    }

    @GetMapping("/search/hashtag")
    public ResponseEntity<Page<AdminPostResponse>>
    searchByHashtag(

            @RequestParam String hashtag,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(
                adminPostService.searchPostsByHashtag(
                        hashtag,
                        page,
                        size
                )
        );
    }

}