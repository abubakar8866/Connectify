package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminDashboardResponse;
import com.abubakar.connectify.dto.response.StorySummaryResponse;
import com.abubakar.connectify.dto.response.UserSummaryResponse;
import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.ReportStatus;
import com.abubakar.connectify.repository.*;
import com.abubakar.connectify.service.AdminDashboardService;

import com.abubakar.connectify.util.AdminValidator;
import com.abubakar.connectify.util.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminDashboardServiceImpl
        implements AdminDashboardService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminDashboardServiceImpl.class
            );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Override
    public AdminDashboardResponse getDashboardData() {

        User admin = authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfDay =
                LocalDate.now()
                        .atStartOfDay();

        logger.info(
                "Fetching admin dashboard analytics | adminId: {}",
                admin.getId()
        );

        // ================= USERS =================
        Long totalUsers =
                userRepository.count();

        Long activeUsers =
                userRepository.countByIsActiveTrue();

        Long bannedUsers =
                userRepository.countByAccountStatus(
                        AccountStatus.BANNED
                );

        Long newUsersToday =
                userRepository.countByCreatedAtAfter(
                        startOfDay
                );

        logger.debug(
                "User analytics fetched | totalUsers: {} | activeUsers: {} | bannedUsers: {} | newUsersToday: {}",
                totalUsers,
                activeUsers,
                bannedUsers,
                newUsersToday
        );

        // ================= POSTS =================
        Long totalPosts =
                postRepository.count();

        Long postsCreatedToday =
                postRepository.countByCreatedAtAfter(
                        startOfDay
                );

        Long deletedPosts =
                postRepository.countByDeletedTrue();

        logger.debug(
                "Post analytics fetched | totalPosts: {} | postsCreatedToday: {} | deletedPosts: {}",
                totalPosts,
                postsCreatedToday,
                deletedPosts
        );

        // ================= ENGAGEMENT =================
        Long totalLikes =
                likeRepository.count();

        Long activeComments =
                commentRepository.countByDeletedFalse();

        Long deletedComments =
                commentRepository.countByDeletedTrue();

        Long totalComments =
                activeComments + deletedComments;

        logger.debug(
                """
                Engagement analytics fetched
                | totalLikes: {}
                | totalComments: {}
                | activeComments: {}
                | deletedComments: {}
                """,
                totalLikes,
                totalComments,
                activeComments,
                deletedComments
        );

        // ================= CHAT ANALYTICS =================
        Long totalChats =
                chatRepository.countByDeletedByAdminFalse();

        Long activeChats =
                chatRepository.countByDeletedByAdminFalseAndIsActiveTrue();

        Long messagesToday =
                messageRepository
                        .countByCreatedAtAfterAndDeletedByAdminFalse(
                                startOfDay
                        );

        logger.debug(
                "Chat analytics fetched | totalChats: {} | activeChats: {} | messagesToday: {}",
                totalChats,
                activeChats,
                messagesToday
        );

        // ================= STORY ANALYTICS =================
        Long totalStories =
                storyRepository.count();

        Long deletedStories =
                storyRepository.countByDeletedTrue();

        Long activeStories =
                storyRepository
                        .countByDeletedFalseAndIsActiveTrueAndExpiresAtAfter(
                                now
                        );

        Long expiredStories =
                storyRepository.countByExpiresAtBeforeAndDeletedFalse(
                        now
                );

        Long restoreRequestsCount =
                storyRepository.countByRestoreRequestedTrue();

        logger.debug(
                "Story analytics fetched | totalStories: {} | activeStories: {} | deletedStories: {} | expiredStories: {} | restoreRequests: {}",
                totalStories,
                activeStories,
                deletedStories,
                expiredStories,
                restoreRequestsCount
        );

        // ================= TOP VIEWED STORIES =================
        List<StorySummaryResponse> topViewedStories =
                storyRepository
                        .findTop10ByDeletedFalseOrderByViewCountDescIdDesc()
                        .stream()
                        .map(this::mapToStorySummary)
                        .toList();

        logger.debug(
                "Top viewed stories fetched | count: {}",
                topViewedStories.size()
        );

        // ================= TOP REACTED STORIES =================
        List<StorySummaryResponse> topReactedStories =
                storyRepository
                        .findTop10ByDeletedFalseOrderByReactionCountDescIdDesc()
                        .stream()
                        .map(this::mapToStorySummary)
                        .toList();

        logger.debug(
                "Top reacted stories fetched | count: {}",
                topReactedStories.size()
        );

        // ================= REPORT ANALYTICS =================

        Long totalReports =
                reportRepository.count();

        Long reportsToday =
                reportRepository.countByCreatedAtAfter(
                        startOfDay
                );

        Long pendingReports =
                reportRepository.countByStatus(
                        ReportStatus.PENDING
                );

        Long reviewedReports =
                reportRepository.countByStatus(
                        ReportStatus.REVIEWED
                );

        Long resolvedReports =
                reportRepository.countByStatus(
                        ReportStatus.RESOLVED
                );

        Long rejectedReports =
                reportRepository.countByStatus(
                        ReportStatus.REJECTED
                );

        logger.debug(
                """
                Report analytics fetched
                | totalReports: {}
                | reportsToday: {}
                | pendingReports: {}
                | reviewedReports: {}
                | resolvedReports: {}
                | rejectedReports: {}
                """,
                totalReports,
                reportsToday,
                pendingReports,
                reviewedReports,
                resolvedReports,
                rejectedReports
        );

        // ================= MOST ACTIVE USERS =================

        List<UserSummaryResponse> mostActiveUsers =
                userRepository.findMostActiveUsers()
                        .stream()
                        .limit(10)
                        .map(this::mapToUserSummary)
                        .toList();

        logger.debug(
                "Most active users fetched | resultSize: {}",
                mostActiveUsers.size()
        );

        logger.info(
                "Admin dashboard analytics fetched successfully | adminId: {}",
                admin.getId()
        );

        // ================= FINAL RESPONSE =================

        return AdminDashboardResponse.builder()

                // USERS
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .bannedUsers(bannedUsers)
                .newUsersToday(newUsersToday)

                // POSTS
                .totalPosts(totalPosts)
                .postsCreatedToday(postsCreatedToday)
                .deletedPosts(deletedPosts)

                // ENGAGEMENT
                .totalLikes(totalLikes)
                .totalComments(totalComments)
                .activeComments(activeComments)
                .deletedComments(deletedComments)

                // CHATS
                .totalChats(totalChats)
                .activeChats(activeChats)
                .messagesToday(messagesToday)

                // STORY ANALYTICS
                .totalStories(totalStories)
                .activeStories(activeStories)
                .deletedStories(deletedStories)
                .expiredStories(expiredStories)
                .restoreRequestsCount(restoreRequestsCount)
                .topViewedStories(topViewedStories)
                .topReactedStories(topReactedStories)

                // REPORT ANALYTICS
                .totalReports(totalReports)
                .reportsToday(reportsToday)
                .pendingReports(pendingReports)
                .reviewedReports(reviewedReports)
                .resolvedReports(resolvedReports)
                .rejectedReports(rejectedReports)

                // ACTIVE USERS
                .mostActiveUsers(mostActiveUsers)

                .build();
    }

    // ================= PRIVATE METHODS =================
    private UserSummaryResponse mapToUserSummary(
            User user
    ) {

        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUname())
                .profileImageUrl(
                        user.getProfileImageUrl()
                )
                .followersCount(
                        user.getFollowersCount()
                )
                .followingCount(
                        user.getFollowingCount()
                )
                .postsCount(
                        (long) user.getPosts().size()
                )
                .build();
    }

    private StorySummaryResponse mapToStorySummary(
            Story story
    ) {

        return StorySummaryResponse.builder()
                .id(story.getId())
                .username(
                        story.getUser().getUname()
                )
                .mediaUrl(
                        story.getMediaUrl()
                )
                .mediaType(
                        story.getMediaType()
                )
                .viewCount(
                        story.getViewCount()
                )
                .reactionCount(
                        story.getReactionCount()
                )
                .build();
    }

}

