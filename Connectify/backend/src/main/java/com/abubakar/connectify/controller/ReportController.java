package com.abubakar.connectify.controller;

import com.abubakar.connectify.dto.request.CreateReportRequest;
import com.abubakar.connectify.dto.response.ReportResponse;
import com.abubakar.connectify.service.ReportService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger =
            LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<ReportResponse> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody CreateReportRequest request
    ) {

        logger.info(
                "Report post API called | postId: {}",
                postId
        );

        ReportResponse response =
                reportService.reportPost(
                        postId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<ReportResponse> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CreateReportRequest request
    ) {

        logger.info(
                "Report comment API called | commentId: {}",
                commentId
        );

        ReportResponse response =
                reportService.reportComment(
                        commentId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ReportResponse> reportUser(
            @PathVariable Long userId,
            @Valid @RequestBody CreateReportRequest request
    ) {

        logger.info(
                "Report user API called | userId: {}",
                userId
        );

        ReportResponse response =
                reportService.reportUser(
                        userId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/chat/{chatId}")
    public ResponseEntity<ReportResponse> reportChat(
            @PathVariable Long chatId,
            @Valid @RequestBody CreateReportRequest request
    ) {

        logger.info(
                "Report chat API called | chatId: {}",
                chatId
        );

        ReportResponse response =
                reportService.reportChat(
                        chatId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/message/{messageId}")
    public ResponseEntity<ReportResponse> reportMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody CreateReportRequest request
    ) {

        logger.info(
                "Report message API called | messageId: {}",
                messageId
        );

        ReportResponse response =
                reportService.reportMessage(
                        messageId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/stories/{storyId}")
    public ResponseEntity<ReportResponse>
    reportStory(
            @PathVariable Long storyId,

            @Valid
            @RequestBody
            CreateReportRequest request
    ) {

        logger.info(
                "Report story API called | storyId: {}",
                storyId
        );

        ReportResponse response =
                reportService.reportStory(
                        storyId,
                        request
                );

        return ResponseEntity.ok(response);

    }

}

