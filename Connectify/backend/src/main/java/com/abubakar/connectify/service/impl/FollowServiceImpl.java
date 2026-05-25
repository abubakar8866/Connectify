package com.abubakar.connectify.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.abubakar.connectify.dto.response.*;
import com.abubakar.connectify.entity.Follow;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.UnauthorizedException;
import com.abubakar.connectify.repository.FollowRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.FollowService;

import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.CursorPaginationUtil;
import com.abubakar.connectify.util.PaginationUtil;
import com.abubakar.connectify.util.UserAccessValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FollowServiceImpl implements FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);

    private static final long VERIFIED_FOLLOWERS_COUNT = 5;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserAccessValidator userAccessValidator;

    // TOGGLE FOLLOW
    @Override
    public FollowResponse toggleFollow(Long userId) {

        logger.info(
                "Toggle follow request received | targetUserId: {}",
                userId
        );

        User currentUser = this.authUtil.getCurrentUser();

        User targetUser = this.userAccessValidator.getValidUser(userId);

        // SELF FOLLOW CHECK
        if (Objects.equals(currentUser.getId(), targetUser.getId())) {

            logger.warn(
                    "User attempted self follow | userId: {}",
                    currentUser.getId()
            );

            throw new UnauthorizedException(
                    "You cannot follow yourself"
            );
        }

        Optional<Follow> existingFollow =
                followRepository.findByFollowerAndFollowing(
                        currentUser,
                        targetUser
                );

        // UNFOLLOW
        if (existingFollow.isPresent()) {

            logger.info(
                    "Unfollowing user | followerId: {} | followingId: {}",
                    currentUser.getId(),
                    targetUser.getId()
            );

            followRepository.delete(existingFollow.get());

            targetUser.setFollowersCount(
                    Math.max(0, targetUser.getFollowersCount() - 1)
            );

            updateVerificationBadge(targetUser);

            currentUser.setFollowingCount(
                    Math.max(
                            0,
                            currentUser.getFollowingCount() - 1
                    )
            );

            userRepository.save(targetUser);
            userRepository.save(currentUser);

            logger.info(
                    "User unfollowed successfully | followerId: {} | followingId: {}",
                    currentUser.getId(),
                    targetUser.getId()
            );

            return FollowResponse.builder()
                    .following(false)
                    .followersCount(targetUser.getFollowersCount())
                    .followingCount(currentUser.getFollowingCount())
                    .build();
        }

        // FOLLOW
        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();

        followRepository.save(follow);

        targetUser.setFollowersCount(
                targetUser.getFollowersCount() + 1
        );

        currentUser.setFollowingCount(
                currentUser.getFollowingCount() + 1
        );

        updateVerificationBadge(targetUser);

        userRepository.save(targetUser);
        userRepository.save(currentUser);

        // CREATE NOTIFICATION
        notificationService.createNotification(
                targetUser.getId(),
                currentUser.getId(),
                currentUser.getUname() + " started following you",
                NotificationType.FOLLOW,
                null,
                null
        );

        logger.info(
                """
                User unfollowed successfully
                | followerId: {}
                | followingId: {}
                | followersCount: {}
                """,
                currentUser.getId(),
                targetUser.getId(),
                targetUser.getFollowersCount()
        );

        return FollowResponse.builder()
                .following(true)
                .followersCount(targetUser.getFollowersCount())
                .followingCount(currentUser.getFollowingCount())
                .build();
    }

    // GET FOLLOWERS
    @Override
    public CursorCountResponse<UserPreviewResponse> getFollowers(
            Long userId,
            Long cursor,
            int size
    )  {

        logger.info(
                """
                Fetching followers
                | userId: {}
                | cursor: {}
                | size: {}
                """,
                userId,
                cursor,
                size
        );

        User targetUser = this.userAccessValidator.getValidUser(userId);

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Follow> followers;

        if (cursor == null) {

            followers =
                    followRepository
                            .findByFollowingOrderByIdDesc(
                                    targetUser,
                                    pageable
                            );

        } else {

            followers =
                    followRepository
                            .findByFollowingAndIdLessThanOrderByIdDesc(
                                    targetUser,
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                """
                Followers fetched successfully
                | userId: {}
                | resultSize: {}
                """,
                userId,
                followers.size()
        );

        CursorPageResponse<UserPreviewResponse> page =
                CursorPaginationUtil.buildResponse(
                        followers,
                        size,
                        Follow::getId,
                        follow ->
                                mapToUserPreviewResponse(
                                        follow.getFollower()
                                )
                );

        return CursorCountResponse
                .<UserPreviewResponse>builder()
                .page(page)
                .totalCount(
                        targetUser.getFollowersCount()
                )
                .build();
    }

    // GET FOLLOWING
    @Override
    public CursorCountResponse<UserPreviewResponse>
    getFollowing(
            Long userId,
            Long cursor,
            int size
    ) {

        logger.info(
                """
                Fetching following list
                | userId: {}
                | cursor: {}
                | size: {}
                """,
                userId,
                cursor,
                size
        );

        User targetUser = this.userAccessValidator.getValidUser(userId);

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Follow> following;

        // FIRST PAGE
        if (cursor == null) {

            following =
                    followRepository
                            .findByFollowerOrderByIdDesc(
                                    targetUser,
                                    pageable
                            );

        }

        // NEXT PAGE
        else {

            following =
                    followRepository
                            .findByFollowerAndIdLessThanOrderByIdDesc(
                                    targetUser,
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                """
                Following fetched successfully
                | userId: {}
                | resultSize: {}
                """,
                userId,
                following.size()
        );

        CursorPageResponse<UserPreviewResponse> page =
                CursorPaginationUtil.buildResponse(
                        following,
                        size,
                        Follow::getId,
                        follow ->
                                mapToUserPreviewResponse(
                                        follow.getFollowing()
                                )
                );

        return CursorCountResponse
                .<UserPreviewResponse>builder()
                .page(page)
                .totalCount(targetUser.getFollowingCount())
                .build();
    }

    // ================= GET FOLLOW COUNTS =================
    @Override
    public FollowCountResponse getFollowCounts(
            Long userId
    ) {

        logger.info(
                "Fetching follow counts | requestedUserId: {}",
                userId
        );

        User targetUser;

        // CURRENT USER
        if (userId == null) {

            targetUser =
                    authUtil.getCurrentUser();

            logger.info(
                    "Fetching follow counts for current user | userId: {}",
                    targetUser.getId()
            );

        }

        // TARGET USER
        else {

            targetUser =
                    userAccessValidator.getValidUser(
                            userId
                    );

            logger.info(
                    "Fetching follow counts for target user | userId: {}",
                    targetUser.getId()
            );
        }

        FollowCountResponse response =
                FollowCountResponse.builder()
                        .userId(targetUser.getId())
                        .followersCount(
                                targetUser.getFollowersCount()
                        )
                        .followingCount(
                                targetUser.getFollowingCount()
                        )
                        .build();

        logger.info(
                """
                Follow counts fetched successfully
                | userId: {}
                | followersCount: {}
                | followingCount: {}
                """,
                targetUser.getId(),
                targetUser.getFollowersCount(),
                targetUser.getFollowingCount()
        );

        return response;
    }

    // PRIVATE METHODS
    private UserPreviewResponse mapToUserPreviewResponse(User user) {

        return UserPreviewResponse.builder()
                .id(user.getId())
                .uname(user.getUname())
                .profileImageUrl(user.getProfileImageUrl())
                .following(isFollowing(user))
                .build();
    }

    private Boolean isFollowing(User targetUser) {

        User currentUser = this.authUtil.getCurrentUser();

        return followRepository
                .findByFollowerAndFollowing(
                        currentUser,
                        targetUser
                )
                .isPresent();
    }

    private void updateVerificationBadge(User user) {

        boolean verified =
                Boolean.TRUE.equals(user.getIsEmailVerified())
                        &&
                        user.getFollowersCount() >= VERIFIED_FOLLOWERS_COUNT;

        user.setIsVerified(verified);

        logger.info(
                "Verification badge updated | userId: {} | verified: {}",
                user.getId(),
                verified
        );
    }

}

