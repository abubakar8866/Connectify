package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.service.AdminPostService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<AdminPostResponse>>
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
            Long cursor,

            @RequestParam(defaultValue = "10")
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
                        cursor,
                        size
                )
        );
    }

}