package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.AdminUserFilter;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReportRepository reportRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(AdminUserServiceImpl.class);

    @Override
    public Page<AdminUserResponse> getUsers(
            int page,
            int size,
            String keyword,
            AdminUserFilter filter
    ) {

        logger.info(
                "Fetching users | page: {} | size: {} | keyword: {} | filter: {}",
                page,
                size,
                keyword,
                filter
        );

        Pageable pageable =
                PageRequest.of(page, size);

        Page<User> users;

        // SEARCH
        if (keyword != null && !keyword.isBlank()) {

            users =
                    userRepository
                            .findByNameContainingIgnoreCaseOrUnameContainingIgnoreCase(
                                    keyword,
                                    keyword,
                                    pageable
                            );
        }

        // FILTERS
        else if (filter != null) {

            users = switch (filter) {

                case ACTIVE ->
                        userRepository.findByAccountStatus(
                                AccountStatus.ACTIVE,
                                pageable
                        );

                case BANNED ->
                        userRepository.findByAccountStatus(
                                AccountStatus.BANNED,
                                pageable
                        );

                case PRIVATE ->
                        userRepository.findByIsPrivate(
                                true,
                                pageable
                        );

                case PUBLIC ->
                        userRepository.findByIsPrivate(
                                false,
                                pageable
                        );

                case VERIFIED ->
                        userRepository.findByIsVerified(
                                true,
                                pageable
                        );
            };

        } else {

            users =
                    userRepository.findAll(pageable);
        }

        logger.info(
                "Users fetched successfully"
        );

        return users.map(this::mapToAdminUserResponse);
    }

    @Override
    public UserDetailsAdminResponse getUserDetails(
            Long userId
    ) {

        logger.info(
                "Fetching user details | userId: {}",
                userId
        );

        User user =
                getUserById(userId);

        Long postsCount =
                postRepository.countByUser(user);

        Long reportsCount =
                reportRepository.countByReportedUser(user);

        logger.info(
                "User details fetched successfully"
        );

        return UserDetailsAdminResponse.builder()

                .id(user.getId())
                .name(user.getName())
                .uname(user.getUname())
                .email(user.getEmail())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())

                .isActive(user.getIsActive())
                .isPrivate(user.getIsPrivate())
                .isVerified(user.getIsVerified())

                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())

                .postsCount(postsCount)
                .reportsCount(reportsCount)

                .accountStatus(user.getAccountStatus())

                .createdAt(user.getCreatedAt())

                .build();
    }

    @Override
    public void banUser(
            Long userId,
            BanUserRequest request
    ) {

        logger.info(
                "Ban user request | userId: {}",
                userId
        );

        User user =
                getUserById(userId);

        user.setAccountStatus(
                AccountStatus.BANNED
        );

        user.setIsActive(false);

        user.setBanReason(
                request.getReason()
        );

        user.setAdminNote(
                request.getAdminNote()
        );

        if (
                Boolean.TRUE.equals(
                        request.getPermanent()
                )
        ) {

            user.setBannedUntil(null);

        } else {

            user.setBannedUntil(
                    LocalDateTime.now()
                            .plusDays(
                                    request.getDurationInDays()
                            )
            );
        }

        userRepository.save(user);

        logger.info(
                "User banned successfully | userId: {}",
                userId
        );
    }

    @Override
    public void unbanUser(Long userId) {

        logger.info(
                "Unban user request | userId: {}",
                userId
        );

        User user =
                getUserById(userId);

        user.setAccountStatus(
                AccountStatus.ACTIVE
        );

        user.setIsActive(true);

        user.setBanReason(null);

        user.setAdminNote(null);

        user.setBannedUntil(null);

        userRepository.save(user);

        logger.info(
                "User unbanned successfully | userId: {}",
                userId
        );
    }

    @Override
    public void deleteUser(Long userId) {

        logger.info(
                "Soft delete user request | userId: {}",
                userId
        );

        User user =
                getUserById(userId);

        user.setIsDeleted(true);

        user.setIsActive(false);

        userRepository.save(user);

        logger.info(
                "User soft deleted successfully | userId: {}",
                userId
        );
    }

    // ================= PRIVATE METHODS =================

    private User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> {

                    logger.error(
                            "User not found | userId: {}",
                            userId
                    );

                    return new ResourceNotFound(
                            "User not found with id: " + userId
                    );
                });
    }

    private AdminUserResponse mapToAdminUserResponse(
            User user
    ) {

        return AdminUserResponse.builder()

                .id(user.getId())
                .name(user.getName())
                .uname(user.getUname())
                .email(user.getEmail())

                .isActive(user.getIsActive())
                .isPrivate(user.getIsPrivate())
                .isVerified(user.getIsVerified())

                .accountStatus(user.getAccountStatus())

                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())

                .createdAt(user.getCreatedAt())

                .build();
    }

}