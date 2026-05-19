package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.HashtagResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.UserSearchResponse;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.Gender;
import com.abubakar.connectify.repository.*;
import com.abubakar.connectify.service.SearchService;
import com.abubakar.connectify.specification.UserSpecification;
import com.abubakar.connectify.util.AuthUtil;

import com.abubakar.connectify.util.CursorPaginationUtil;
import com.abubakar.connectify.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private AuthUtil authUtil;

    private static final Logger logger =
            LoggerFactory.getLogger(SearchServiceImpl.class);

    // ================= SEARCH USERS =================
    @Override
    public CursorPageResponse<UserSearchResponse> searchUsers(

            String keyword,
            Boolean verified,
            Boolean emailVerified,
            Boolean isPrivate,
            Boolean active,
            AccountStatus status,
            String city,
            Gender gender,
            Long minFollowers,
            Long cursor,
            int size
    ) {

        logger.info("Searching users with cursor pagination");

        User currentUser =
                authUtil.getCurrentUser();

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        Specification<User> specification =

                Specification
                        .where(
                                UserSpecification.searchByKeyword(keyword)
                        )
                        .and(
                                UserSpecification.hasVerified(verified)
                        )
                        .and(
                                UserSpecification.hasEmailVerified(
                                        emailVerified
                                )
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
                        )
                        .and(
                                UserSpecification.excludeCurrentUser(
                                        currentUser.getId()
                                )
                        )
                        .and(
                                UserSpecification.cursor(cursor)
                        );

        Page<User> users =
                userRepository.findAll(
                        specification,
                        pageable
                );

        return CursorPaginationUtil.buildResponse(
                users.getContent(),
                size,
                User::getId,
                user ->
                        mapToUserSearchResponse(
                                user,
                                currentUser
                        )
        );
    }

    // ================= SEARCH HASHTAGS =================
    @Override
    public CursorPageResponse<HashtagResponse> searchHashtags(
            String keyword,
            Long cursor,
            int size
    ) {

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Hashtag> hashtags;

        if (cursor == null) {

            hashtags =
                    hashtagRepository
                            .findByNameContainingIgnoreCaseOrderByIdDesc(
                                    keyword,
                                    pageable
                            );

        } else {

            hashtags =
                    hashtagRepository
                            .findByNameContainingIgnoreCaseAndIdLessThanOrderByIdDesc(
                                    keyword,
                                    cursor,
                                    pageable
                            );
        }

        return CursorPaginationUtil.buildResponse(
                hashtags,
                size,
                Hashtag::getId,
                this::mapToHashtagResponse
        );
    }

    // ================= TRENDING POSTS =================
    @Override
    public CursorPageResponse<PostResponse> getTrendingPosts(
            Long cursor,
            int size
    ) {

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Post> posts;

        if (cursor == null) {

            posts =
                    postRepository
                            .findByDeletedFalseOrderByLikeCountDescCommentCountDescIdDesc(
                                    pageable
                            );

        } else {

            posts =
                    postRepository
                            .findByDeletedFalseAndIdLessThanOrderByLikeCountDescCommentCountDescIdDesc(
                                    cursor,
                                    pageable
                            );
        }

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                this::mapToPostResponse
        );
    }

    // ================= SUGGESTED USERS =================
    @Override
    public CursorPageResponse<UserSearchResponse> getSuggestedUsers(
            Long cursor,
            int size
    ) {

        User currentUser =
                authUtil.getCurrentUser();

        List<Follow> following =
                followRepository.findByFollower(currentUser);

        List<Long> excludedIds =
                new ArrayList<>();

        excludedIds.add(currentUser.getId());

        for (Follow follow : following) {

            excludedIds.add(
                    follow.getFollowing().getId()
            );
        }

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<User> users;

        if (cursor == null) {

            users =
                    userRepository
                            .findByIdNotInOrderByFollowersCountDesc(
                                    excludedIds,
                                    pageable
                            );

        } else {

            users =
                    userRepository
                            .findByIdNotInAndIdLessThanOrderByFollowersCountDesc(
                                    excludedIds,
                                    cursor,
                                    pageable
                            );
        }

        return CursorPaginationUtil.buildResponse(
                users,
                size,
                User::getId,
                user ->
                        mapToUserSearchResponse(
                                user,
                                currentUser
                        )
        );
    }

    // ================= PRIVATE METHODS =================

    private UserSearchResponse mapToUserSearchResponse(
            User user,
            User currentUser
    ) {

        boolean following =
                followRepository
                        .findByFollowerAndFollowing(
                                currentUser,
                                user
                        )
                        .isPresent();

        return UserSearchResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .uname(user.getUname())
                .profileImageUrl(user.getProfileImageUrl())
                .followersCount(user.getFollowersCount())
                .following(following)
                .build();
    }

    private HashtagResponse mapToHashtagResponse(
            Hashtag hashtag
    ) {

        return HashtagResponse.builder()
                .id(hashtag.getId())
                .name(hashtag.getName())
                .postCount((long) hashtag.getPosts().size())
                .build();
    }

    private PostResponse mapToPostResponse(
            Post post
    ) {

        boolean liked =
                post.getLikes()
                        .stream()
                        .anyMatch(like ->
                                like.getUser()
                                        .getId()
                                        .equals(
                                                authUtil
                                                        .getCurrentUser()
                                                        .getId()
                                        )
                        );

        return PostResponse.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .liked(liked)
                .username(post.getUser().getUname())
                .createdAt(post.getCreatedAt())

                .mediaUrls(
                        post.getMediaList()
                                .stream()
                                .map(media -> media.getUrl())
                                .toList()
                )

                .hashtags(
                        post.getHashtags()
                                .stream()
                                .map(Hashtag::getName)
                                .toList()
                )

                .build();
    }

}

