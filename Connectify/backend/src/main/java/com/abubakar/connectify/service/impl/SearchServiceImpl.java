package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.UserSearchRequest;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private LikeRepository likeRepository;

    @Autowired
    private AuthUtil authUtil;

    private static final Logger logger =
            LoggerFactory.getLogger(SearchServiceImpl.class);

    // ================= SEARCH USERS =================
    @Override
    public CursorPageResponse<UserSearchResponse> searchUsers(
            UserSearchRequest request,
            Long cursor,
            int size
    ) {

        logger.info(
                """
                Search users request
                | keyword: {}
                | verified: {}
                | emailVerified: {}
                | isPrivate: {}
                | city: {}
                | gender: {}
                | minFollowers: {}
                | cursor: {}
                | size: {}
                """,
                request.getKeyword(),
                request.getVerified(),
                request.getEmailVerified(),
                request.getIsPrivate(),
                request.getCity(),
                request.getGender(),
                request.getMinFollowers(),
                cursor,
                size
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Authenticated user for search | userId: {}",
                currentUser.getId()
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        Specification<User> specification =

                Specification
                        .where(
                                UserSpecification.searchByKeyword(request.getKeyword())
                        )
                        .and(
                                UserSpecification.hasVerified(request.getVerified())
                        )
                        .and(
                                UserSpecification.hasEmailVerified(
                                        request.getEmailVerified()
                                )
                        )
                        .and(
                                UserSpecification.hasPrivateAccount(request.getIsPrivate())
                        )
                        .and(
                                UserSpecification.hasCity(request.getCity())
                        )
                        .and(
                                UserSpecification.hasGender(request.getGender())
                        )
                        .and(
                                UserSpecification.hasMinFollowers(request.getMinFollowers())
                        )
                        .and(
                                UserSpecification.excludeCurrentUser(
                                        currentUser.getId()
                                )
                        )
                        .and(
                                UserSpecification.cursor(cursor)
                        ).and(
                                UserSpecification.hasDeleted(false)
                        )
                        .and(
                                UserSpecification.hasActive(true)
                        )
                        .and(
                                UserSpecification.hasAccountStatus(AccountStatus.ACTIVE)
                        );

        Page<User> users =
                userRepository.findAll(
                        specification,
                        pageable
                );

        List<User> userList = users.getContent();

        logger.info(
                "Users fetched successfully | count: {}",
                userList.size()
        );

        List<Long> userIds =
                userList.stream()
                        .map(User::getId)
                        .toList();

        Set<Long> followingIds =
                userIds.isEmpty()
                        ? Set.of()
                        : followRepository.findFollowingIds(
                        currentUser,
                        userIds
                );

        logger.debug(
                "Following IDs fetched inside search User method | count: {}",
                followingIds.size()
        );

        return CursorPaginationUtil.buildResponse(
                userList,
                size,
                User::getId,
                user -> mapToUserSearchResponse(
                        user,
                        followingIds
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

        logger.info(
                "Search hashtags request | keyword: {} | cursor: {} | size: {}",
                keyword,
                cursor,
                size
        );

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

        logger.info(
                "Hashtags fetched successfully | count: {}",
                hashtags.size()
        );

        return CursorPaginationUtil.buildResponse(
                hashtags,
                size,
                Hashtag::getId,
                this::mapToHashtagResponse
        );
    }

    // ================= POSTS BY HASHTAG =================
    @Override
    public CursorPageResponse<PostResponse> getPostsByHashtag(
            String hashtagName,
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching posts by hashtag | hashtag: {} | cursor: {} | size: {}",
                hashtagName,
                cursor,
                size
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Authenticated user for hashtag posts | userId: {}",
                currentUser.getId()
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Post> posts;

        if (cursor == null) {

            posts =
                    postRepository
                            .findByHashtags_NameAndDeletedFalseAndUserDeletedFalseAndUserIsActiveTrueAndUserAccountStatusNotOrderByIdDesc(
                                    hashtagName,
                                    AccountStatus.BANNED,
                                    pageable
                            );

        } else {

            posts =
                    postRepository
                            .findByHashtags_NameAndDeletedFalseAndUserDeletedFalseAndUserIsActiveTrueAndIdLessThanAndUserAccountStatusNotOrderByIdDesc(
                                    hashtagName,
                                    cursor,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        logger.info(
                "Posts fetched successfully by hashtag | hashtag: {} | count: {}",
                hashtagName,
                posts.size()
        );

        List<Long> postIds =
                posts.stream()
                        .map(Post::getId)
                        .toList();

        Set<Long> likedPostIds =
                postIds.isEmpty()
                        ? Set.of()
                        : likeRepository.findLikedPostIdsByUserAndPostIds(
                        currentUser,
                        postIds
                );

        logger.debug(
                "Liked post IDs fetched inside getPostByHashtag method | count: {}",
                likedPostIds.size()
        );

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                post -> mapToPostResponse(
                        post,
                        likedPostIds,
                        currentUser
                )
        );
    }

    // ================= TRENDING POSTS =================
    @Override
    public CursorPageResponse<PostResponse> getTrendingPosts(
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching trending posts | cursor: {} | size: {}",
                cursor,
                size
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Authenticated user for trending posts | userId: {}",
                currentUser.getId()
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Post> posts;

        if (cursor == null) {

            posts =
                    postRepository
                            .findByDeletedFalseAndUserDeletedFalseAndUserIsActiveTrueAndUserAccountStatusNotOrderByLikeCountDescCommentCountDescIdDesc(
                                    AccountStatus.BANNED,
                                    pageable
                            );

        } else {

            posts =
                    postRepository
                            .findByDeletedFalseAndUserDeletedFalseAndUserIsActiveTrueAndIdLessThanAndUserAccountStatusNotOrderByLikeCountDescCommentCountDescIdDesc(
                                    cursor,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        logger.info(
                "Trending posts fetched successfully | count: {}",
                posts.size()
        );

        List<Long> postIds =
                posts.stream()
                        .map(Post::getId)
                        .toList();

        Set<Long> likedPostIds =
                postIds.isEmpty()
                        ? Set.of()
                        : likeRepository.findLikedPostIdsByUserAndPostIds(
                        currentUser,
                        postIds
                );

        logger.debug(
                "Liked post IDs fetched | count: {}",
                likedPostIds.size()
        );

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                post -> mapToPostResponse(
                        post,
                        likedPostIds,
                        currentUser
                )
        );
    }

    // ================= SUGGESTED USERS =================
    @Override
    public CursorPageResponse<UserSearchResponse> getSuggestedUsers(
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching suggested users | cursor: {} | size: {}",
                cursor,
                size
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Authenticated user for suggestions | userId: {}",
                currentUser.getId()
        );

        List<Long> excludedIds =
                new ArrayList<>(
                        followRepository.findFollowingIdsByFollower(
                                currentUser
                        )
                );

        excludedIds.add(currentUser.getId());

        logger.debug(
                "Excluded user IDs prepared | count: {}",
                excludedIds.size()
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<User> users;

        // FIRST PAGE
        if (cursor == null) {

            users =
                    userRepository
                            .findByIdNotInAndDeletedFalseAndIsActiveTrueAndAccountStatusNotOrderByFollowersCountDesc(
                                    excludedIds,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        // NEXT PAGE
        else {

            users =
                    userRepository
                            .findByIdNotInAndDeletedFalseAndIsActiveTrueAndAccountStatusNotAndIdLessThanOrderByFollowersCountDesc(
                                    excludedIds,
                                    AccountStatus.BANNED,
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Suggested users fetched successfully | count: {}",
                users.size()
        );

        List<Long> userIds =
                users.stream()
                        .map(User::getId)
                        .toList();

        Set<Long> followingIds =
                userIds.isEmpty()
                    ? Set.of()
                    : followRepository.findFollowingIds(
                        currentUser,
                        userIds
                    );

        logger.debug(
                "Following IDs fetched | count: {}",
                followingIds.size()
        );

        return CursorPaginationUtil.buildResponse(
                users,
                size,
                User::getId,
                user -> mapToUserSearchResponse(
                        user,
                        followingIds
                )
        );
    }

    // ================= PRIVATE METHODS =================
    private UserSearchResponse mapToUserSearchResponse(
            User user,
            Set<Long> followingIds
    ) {

        boolean following =
                followingIds.contains(
                        user.getId()
                );

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
                .postCount(hashtag.getPostCount())
                .build();
    }

    private PostResponse mapToPostResponse(
            Post post,
            Set<Long> likedPostIds,
            User currentUser
    ) {

        return PostResponse.builder()

                .id(post.getId())

                .caption(post.getCaption())

                .userId(
                        post.getUser().getId()
                )

                .username(
                        post.getUser().getUname()
                )

                .userProfileImage(
                        post.getUser().getProfileImageUrl()
                )

                .isVerified(
                        post.getUser().getIsVerified()
                )

                .likeCount(
                        post.getLikeCount()
                )

                .commentCount(
                        post.getCommentCount()
                )

                .liked(
                        likedPostIds.contains(
                                post.getId()
                        )
                )

                .mine(
                        post.getUser()
                                .getId()
                                .equals(currentUser.getId())
                )

                .createdAt(
                        post.getCreatedAt()
                )

                .updatedAt(
                        post.getUpdatedAt()
                )

                .mediaUrls(
                        post.getMediaList()
                                .stream()
                                .map(Media::getUrl)
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

