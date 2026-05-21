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

    private Long totalComments;

    // CHAT ANALYTICS
    private Long totalChats;

    private Long activeChats;

    private Long messagesToday;

    // MOST ACTIVE USERS
    private List<UserSummaryResponse> mostActiveUsers;

}

