package com.abubakar.connectify.service;

import com.abubakar.connectify.dto.request.CreateReportRequest;
import com.abubakar.connectify.dto.response.ReportResponse;

public interface ReportService {

    ReportResponse reportPost(
            Long postId,
            CreateReportRequest request
    );

    ReportResponse reportComment(
            Long commentId,
            CreateReportRequest request
    );

    ReportResponse reportUser(
            Long userId,
            CreateReportRequest request
    );

    ReportResponse reportChat(
            Long chatId,
            CreateReportRequest request
    );

    ReportResponse reportMessage(
            Long messageId,
            CreateReportRequest request
    );

    ReportResponse reportStory(
            Long storyId,
            CreateReportRequest request
    );

}

