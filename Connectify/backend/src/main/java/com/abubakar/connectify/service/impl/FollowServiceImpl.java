package com.abubakar.connectify.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.abubakar.connectify.dto.response.FollowResponse;
import com.abubakar.connectify.dto.response.UserPreviewResponse;
import com.abubakar.connectify.entity.Follow;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.exception.UnauthorizedException;
import com.abubakar.connectify.repository.FollowRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.FollowService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FollowServiceImpl implements FollowService {

    private static final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    // TOGGLE FOLLOW
    @Override
    public FollowResponse toggleFollow(Long userId) {

        logger.info(
                "Toggle follow request received | targetUserId: {}",
                userId
        );

        User currentUser = getCurrentUser();

        User targetUser = getUserById(userId);

        // SELF FOLLOW CHECK
        if (Objects.equals(currentUser.getId(), targetUser.getId())) {

            logger.warn(
                    "User attempted to follow himself | userId: {}",
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

        // =========================================
        // UNFOLLOW
        // =========================================

        if (existingFollow.isPresent()) {

            logger.info(
                    "Unfollowing user | followerId: {} | followingId: {}",
                    currentUser.getId(),
                    targetUser.getId()
            );

            followRepository.delete(existingFollow.get());

            targetUser.setFollowersCount(
                    targetUser.getFollowersCount() - 1
            );

            currentUser.setFollowingCount(
                    currentUser.getFollowingCount() - 1
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

        // =========================================
        // FOLLOW
        // =========================================

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

        userRepository.save(targetUser);
        userRepository.save(currentUser);

        logger.info(
                "User followed successfully | followerId: {} | followingId: {}",
                currentUser.getId(),
                targetUser.getId()
        );

        return FollowResponse.builder()
                .following(true)
                .followersCount(targetUser.getFollowersCount())
                .followingCount(currentUser.getFollowingCount())
                .build();
    }

    // GET FOLLOWERS
    @Override
    public List<UserPreviewResponse> getFollowers(Long userId) {

        logger.info(
                "Fetching followers list | userId: {}",
                userId
        );

        User targetUser = getUserById(userId);

        List<Follow> followers =
                followRepository.findByFollowing(targetUser);

        logger.info(
                "Total followers fetched: {}",
                followers.size()
        );

        return followers.stream()
                .map(follow ->
                        mapToUserPreviewResponse(
                                follow.getFollower()
                        )
                )
                .toList();
    }

    // GET FOLLOWING
    @Override
    public List<UserPreviewResponse> getFollowing(Long userId) {

        logger.info(
                "Fetching following list | userId: {}",
                userId
        );

        User targetUser = getUserById(userId);

        List<Follow> following =
                followRepository.findByFollower(targetUser);

        logger.info(
                "Total following fetched: {}",
                following.size()
        );

        return following.stream()
                .map(follow ->
                        mapToUserPreviewResponse(
                                follow.getFollowing()
                        )
                )
                .toList();
    }

    // PRIVATE METHODS
    private UserPreviewResponse mapToUserPreviewResponse(User user) {

        return UserPreviewResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImage(user.getProfileImageUrl())
                .following(isFollowing(user))
                .build();
    }

    private Boolean isFollowing(User targetUser) {

        User currentUser = getCurrentUser();

        return followRepository
                .findByFollowerAndFollowing(
                        currentUser,
                        targetUser
                )
                .isPresent();
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                Objects.equals(
                        authentication.getPrincipal(),
                        "anonymousUser"
                )) {

            logger.warn("Unauthorized access attempt");

            throw new UnauthorizedException(
                    "User not authenticated"
            );
        }

        User user = (User) authentication.getPrincipal();

        logger.debug(
                "Authenticated user fetched | userId: {}",
                user != null ? user.getId() : null
        );

        return user;
    }

    private User getUserById(Long userId) {

        logger.debug(
                "Fetching user by id: {}",
                userId
        );

        return userRepository.findById(userId)
                .orElseThrow(() -> {

                    logger.error(
                            "User not found with id: {}",
                            userId
                    );

                    return new ResourceNotFound(
                            "User not found with id: " + userId
                    );
                });
    }

}

