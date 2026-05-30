package com.abubakar.connectify.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {

    // USER ANALYTICS
    private Long totalUsers;

    private Long activeUsers;

    private Long newUsersToday;

    private Long bannedUsers;

    // POST ANALYTICS
    private Long totalPosts;

    private Long postsCreatedToday;

    private Long deletedPosts;

    // ENGAGEMENT
    private Long totalLikes;

    private Long activeComments;

    private Long deletedComments;

    private Long totalComments;

    // CHAT ANALYTICS
    private Long totalChats;

    private Long activeChats;

    private Long messagesToday;

    // STORY ANALYTICS
    private Long totalStories;

    private Long activeStories;

    private Long deletedStories;

    private Long expiredStories;

    private Long restoreRequestsCount;

    // REPORT ANALYTICS
    private Long totalReports;

    private Long reportsToday;

    private Long pendingReports;

    private Long reviewedReports;

    private Long resolvedReports;

    private Long rejectedReports;

    private List<StorySummaryResponse> topViewedStories;

    private List<StorySummaryResponse> topReactedStories;

    // MOST ACTIVE USERS
    private List<UserSummaryResponse> mostActiveUsers;

}

