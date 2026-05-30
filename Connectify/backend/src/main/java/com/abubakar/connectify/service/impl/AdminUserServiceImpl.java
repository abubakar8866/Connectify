package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.AdminUserSearchRequest;
import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.AdminUserService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.service.RefreshTokenService;
import com.abubakar.connectify.specification.UserSpecification;

import com.abubakar.connectify.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminUserServiceImpl
        implements AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Autowired
    private UserAccessValidator userAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminUserServiceImpl.class
            );

    @Override
    public CursorPageResponse<AdminUserResponse> getUsers(
            AdminUserSearchRequest request,
            Long cursor,
            int size
    ) {

        User admin = authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.debug(
                "Fetching users for admin panel | adminId: {} | cursor: {} | size: {}",
                admin.getId(),
                cursor,
                size
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        Specification<User> specification =
                Specification
                        .where(
                                UserSpecification.cursor(cursor)
                        )
                        .and(
                                UserSpecification.searchByKeyword(
                                        request.getKeyword()
                                )
                        )
                        .and(
                                UserSpecification.hasVerified(
                                        request.getVerified()
                                )
                        )
                        .and(
                                UserSpecification.hasEmailVerified(
                                        request.getEmailVerified()
                                )
                        )
                        .and(
                                UserSpecification.hasPrivateAccount(
                                        request.getIsPrivate()
                                )
                        )
                        .and(
                                UserSpecification.hasActive(
                                        request.getActive()
                                )
                        )
                        .and(
                                UserSpecification.hasAccountStatus(
                                        request.getStatus()
                                )
                        )
                        .and(
                                UserSpecification.hasCity(
                                        request.getCity()
                                )
                        )
                        .and(
                                UserSpecification.hasGender(
                                        request.getGender()
                                )
                        )
                        .and(
                                UserSpecification.hasMinFollowers(
                                        request.getMinFollowers()
                                )
                        )
                        .and(
                                UserSpecification.restoreRequested(
                                        request.getRestoreRequested()
                                )
                        )
                        .and(
                                UserSpecification.unbanRequested(
                                        request.getUnbanRequested()
                                )
                        )
                        .and(
                                UserSpecification.hasDeleted(
                                        request.getDeleted()
                                )
                        ).and(
                                UserSpecification.reportedOnly(
                                        request.getReportedOnly()
                                )
                        );

        List<User> users =
                userRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .getContent();

        logger.info(
                "Users fetched successfully | adminId: {} | resultSize: {}",
                admin.getId(),
                users.size()
        );

        return CursorPaginationUtil.buildResponse(
                users,
                size,
                User::getId,
                this::mapToAdminUserResponse
        );
    }

    @Override
    public UserDetailsAdminResponse getUserDetails(
            Long userId
    ) {

        User admin = authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.debug(
                "Fetching user details | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );

        User user = userAccessValidator.getValidUser(userId);

        Long postsCount =
                postRepository.countByUser(user);

        Long reportsCount =
                reportRepository.countByReportedUser(user);

        logger.info(
                "User details fetched successfully | targetUserId: {}",
                userId
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
    public void moderateUser(
            Long userId,
            BanUserRequest request
    ) {

        User admin = authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.debug(
                "Ban user request received | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );

        User user = userAccessValidator.getValidUser(userId);

        validateSelfAction(admin, user, "ban");

        // VALIDATION
        if (
                !Boolean.TRUE.equals(request.getPermanent())
                        &&
                        request.getDurationInDays() == null
        ) {

            logger.warn(
                    "Ban user failed - duration missing for temporary ban | targetUserId: {}",
                    userId
            );

            throw new OperationFailException(
                    "Ban duration is required for temporary ban"
            );
        }


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

        if (Boolean.TRUE.equals(
                request.getPermanent()
        )) {

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

        refreshTokenService.deleteByUser(user);

        notificationService.createNotification(
                user.getId(),
                admin.getId(),
                "Your account has been banned by admin.",
                NotificationType.ACCOUNT_BANNED,
                null,
                null
        );

        logger.info(
                "User banned successfully | adminId: {} | targetUserId: {} | permanent: {}",
                admin.getId(),
                userId,
                request.getPermanent()
        );

    }

    @Override
    public void approveUserRestore(Long userId) {

        User admin =
                authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.debug(
                "Restore user request received | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );

        User user = userAccessValidator.getValidUser(userId);

        if (!Boolean.TRUE.equals(user.getRestoreRequested())) {

            logger.warn("Restore request not found");

            throw new OperationFailException(
                    "Restore request not found"
            );

        }

        validateSelfAction(admin, user, "restore");

        user.setDeleted(false);

        user.setIsActive(true);

        user.setAccountStatus(AccountStatus.ACTIVE);

        user.setRestoreRequested(false);
        user.setUnbanRequested(false);

        user.setUnbanAppealMessage(null);
        user.setRestoredAt(LocalDateTime.now());
        userRepository.save(user);

        notificationService.createNotification(
                user.getId(),
                admin.getId(),
                "Your account has been restored.",
                NotificationType.ACCOUNT_RESTORED,
                null,
                null
        );

        logger.info(
                "User restored successfully | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );
    }

    // ================= REJECT RESTORE REQUEST =================
    @Override
    public void rejectUserRestore(
            Long userId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.debug(
                "Reject restore request initiated | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );

        User user = userAccessValidator.getValidUser(userId);

        validateSelfAction(admin, user, "reject unban request");

        validateSelfAction(
                admin,
                user,
                "reject restore request"
        );

        user.setRestoreRequested(false);

        userRepository.save(user);

        notificationService.createNotification(
                user.getId(),
                admin.getId(),
                "Your account restore request has been rejected.",
                NotificationType.RESTORE_REQUEST_REJECTED,
                null,
                null
        );

        logger.info(
                "Restore request rejected successfully | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );
    }

    @Override
    public void approveUserUnban(Long userId) {

        User admin =
                authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.debug(
                "Approve unban request initiated | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );

        User user = userAccessValidator.getValidUser(userId);

        if (!Boolean.TRUE.equals(user.getUnbanRequested())) {

            logger.warn("Unban request not found");

            throw new OperationFailException(
                    "Unban request not found"
            );

        }

        validateSelfAction(admin, user, "approve unban request");

        user.setAccountStatus(AccountStatus.ACTIVE);

        user.setIsActive(true);

        user.setBanReason(null);

        user.setAdminNote(null);

        user.setBannedUntil(null);

        user.setRestoreRequested(false);
        user.setUnbanRequested(false);

        user.setUnbanAppealMessage(null);

        userRepository.save(user);

        notificationService.createNotification(
                user.getId(),
                admin.getId(),
                "Your unban request has been approved.",
                NotificationType.UNBAN_APPROVED,
                null,
                null
        );

        logger.info(
                "Unban request approved successfully | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );
    }

    @Override
    public void rejectUserUnban(Long userId) {

        User admin =
                authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.debug(
                "Reject unban request initiated | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );

        User user = userAccessValidator.getValidUser(userId);

        user.setAdminNote(
                "Your unban request was rejected by admin."
        );

        user.setRestoreRequested(false);
        user.setUnbanRequested(false);

        user.setUnbanAppealMessage(null);

        userRepository.save(user);

        notificationService.createNotification(
                user.getId(),
                admin.getId(),
                "Your unban request has been rejected.",
                NotificationType.UNBAN_REJECTED,
                null,
                null
        );

        logger.info(
                "Unban request rejected successfully | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );
    }

    @Override
    public void permanentlyDeleteUser(Long userId) {

        User admin =
                authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.debug(
                "Delete user request received | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );

        User user = userAccessValidator.getValidUser(userId);

        validateSelfAction(admin, user, "delete");

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setIsActive(false);
        user.setAccountStatus(AccountStatus.DEACTIVATED);

        userRepository.save(user);

        refreshTokenService.deleteByUser(user);

        notificationService.createNotification(
                user.getId(),
                admin.getId(),
                "Your account has been deleted by admin.",
                NotificationType.ACCOUNT_DELETED,
                null,
                null
        );

        logger.info(
                "User deleted successfully | adminId: {} | targetUserId: {}",
                admin.getId(),
                userId
        );

    }

    // ================= PRIVATE =================
    private void validateSelfAction(
            User admin,
            User targetUser,
            String action
    ) {

        if (admin.getId().equals(targetUser.getId())) {

            logger.warn(
                    "Admin attempted self action | adminId: {} | action: {}",
                    admin.getId(),
                    action
            );

            throw new OperationFailException(
                    "Admin cannot " + action + " own account"
            );
        }

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
                .isEmailVerified(user.getIsEmailVerified())

                .accountStatus(user.getAccountStatus())

                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())

                .createdAt(user.getCreatedAt())

                .build();
    }

}

