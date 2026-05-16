package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.BanUserRequest;
import com.abubakar.connectify.dto.response.AdminUserResponse;
import com.abubakar.connectify.dto.response.UserDetailsAdminResponse;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.AdminUserService;
import com.abubakar.connectify.specification.UserSpecification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private PostRepository postRepository;

    @Autowired
    private ReportRepository reportRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminUserServiceImpl.class
            );

    @Override
    public List<AdminUserResponse> getUsers(
            Long cursor,
            int size,
            String keyword,
            Boolean verified,
            Boolean isPrivate,
            Boolean active,
            AccountStatus status,
            String city,
            Gender gender,
            Long minFollowers
    ) {

        logger.info(
                "Fetching admin users"
        );

        Pageable pageable =
                PageRequest.of(
                        0,
                        size,
                        Sort.by(Sort.Direction.DESC, "id")
                );

        Specification<User> specification =
                Specification
                        .where(
                                UserSpecification.cursor(cursor)
                        )
                        .and(
                                UserSpecification.searchByKeyword(keyword)
                        )
                        .and(
                                UserSpecification.hasVerified(verified)
                        )
                        .and(
                                UserSpecification.hasPrivateAccount(isPrivate)
                        )
                        .and(
                                UserSpecification.hasActive(active)
                        )
                        .and(
                                UserSpecification.hasAccountStatus(status)
                        )
                        .and(
                                UserSpecification.hasCity(city)
                        )
                        .and(
                                UserSpecification.hasGender(gender)
                        )
                        .and(
                                UserSpecification.hasMinFollowers(minFollowers)
                        );

        List<User> users =
                userRepository
                        .findAll(specification, pageable)
                        .getContent();

        return users.stream()
                .map(this::mapToAdminUserResponse)
                .toList();
    }

    @Override
    public List<AdminUserResponse> getReportedUsers(
            Long cursor,
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        0,
                        size,
                        Sort.by(Sort.Direction.DESC, "id")
                );

        List<User> users;

        if (cursor == null) {

            users =
                    userRepository
                            .findReportedUsers(pageable)
                            .getContent();

        } else {

            users =
                    userRepository
                            .findReportedUsersByCursor(
                                    cursor,
                                    pageable
                            );
        }

        return users.stream()
                .map(this::mapToAdminUserResponse)
                .toList();
    }

    @Override
    public UserDetailsAdminResponse getUserDetails(
            Long userId
    ) {

        User user = getUserById(userId);

        Long postsCount =
                postRepository.countByUser(user);

        Long reportsCount =
                reportRepository.countByReportedUser(user);

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

        User user = getUserById(userId);

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
    }

    @Override
    public void unbanUser(Long userId) {

        User user = getUserById(userId);

        user.setAccountStatus(
                AccountStatus.ACTIVE
        );

        user.setIsActive(true);

        user.setBanReason(null);

        user.setAdminNote(null);

        user.setBannedUntil(null);

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {

        User user = getUserById(userId);

        user.setIsDeleted(true);

        user.setIsActive(false);

        userRepository.save(user);
    }

    // ================= PRIVATE =================

    private User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "User not found"
                        )
                );
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
