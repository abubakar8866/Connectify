package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminDashboardResponse;
import com.abubakar.connectify.dto.response.UserSummaryResponse;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
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
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Override
    public AdminDashboardResponse getDashboardData() {

        User admin = authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.info("Fetching admin dashboard analytics");

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
                        LocalDateTime.now().minusDays(1)
                );

        logger.info(
                "User analytics fetched successfully"
        );

        // ================= POSTS =================

        Long totalPosts =
                postRepository.count();

        Long postsCreatedToday =
                postRepository.countByCreatedAtAfter(
                        LocalDateTime.now().minusDays(1)
                );

        Long deletedPosts =
                postRepository.countByDeletedTrue();

        logger.info(
                "Post analytics fetched successfully"
        );

        // ================= ENGAGEMENT =================

        Long totalLikes =
                likeRepository.count();

        Long totalComments =
                commentRepository.countByDeletedFalse();

        logger.info(
                "Engagement analytics fetched successfully"
        );

        // ================= CHAT ANALYTICS =================

        Long totalChats =
                chatRepository.countByDeletedByAdminFalse();

        Long activeChats =
                chatRepository.countByDeletedByAdminFalseAndIsActiveTrue();

        LocalDateTime startOfDay = LocalDate.now()
                .atStartOfDay();

        Long messagesToday =
                messageRepository
                        .countByCreatedAtAfterAndDeletedByAdminFalse(
                                startOfDay
                        );

        logger.info("Chat analytics fetched successfully");

        // ================= MOST ACTIVE USERS =================

        List<UserSummaryResponse> mostActiveUsers =
                userRepository.findMostActiveUsers()
                        .stream()
                        .limit(10)
                        .map(this::mapToUserSummary)
                        .toList();

        logger.info(
                "Most active users fetched successfully"
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

                // CHATS
                .totalChats(totalChats)
                .activeChats(activeChats)
                .messagesToday(messagesToday)

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

}

