package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.HashtagResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.UserSearchResponse;
import com.abubakar.connectify.entity.Follow;
import com.abubakar.connectify.entity.Hashtag;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.FollowRepository;
import com.abubakar.connectify.repository.HashtagRepository;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.SearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private static final Logger logger =
            LoggerFactory.getLogger(SearchServiceImpl.class);

    // ================= SEARCH USERS =================
    @Override
    public List<UserSearchResponse> searchUsers(String keyword) {

        logger.info(
                "Searching users with keyword: {}",
                keyword
        );

        User currentUser = getCurrentUser();

        List<User> users =
                userRepository
                        .findByUnameContainingIgnoreCaseOrNameContainingIgnoreCase(
                                keyword,
                                keyword
                        );

        logger.info(
                "Users found: {}",
                users.size()
        );

        return users.stream()

                // EXCLUDE CURRENT USER
                .filter(user ->
                        !user.getId().equals(currentUser.getId())
                )

                .map(user ->
                        mapToUserSearchResponse(
                                user,
                                currentUser
                        )
                )

                .toList();
    }

    // ================= SEARCH HASHTAGS =================
    @Override
    public List<HashtagResponse> searchHashtags(
            String keyword
    ) {

        logger.info(
                "Searching hashtags with keyword: {}",
                keyword
        );

        List<Hashtag> hashtags =
                hashtagRepository
                        .findByNameContainingIgnoreCase(keyword);

        logger.info(
                "Hashtags found: {}",
                hashtags.size()
        );

        return hashtags.stream()
                .map(this::mapToHashtagResponse)
                .toList();
    }

    // ================= TRENDING POSTS =================
    @Override
    public List<PostResponse> getTrendingPosts() {

        logger.info("Fetching trending posts");

        List<Post> posts =
                postRepository
                        .findTop20ByOrderByLikeCountDescCommentCountDesc();

        logger.info(
                "Trending posts fetched: {}",
                posts.size()
        );

        return posts.stream()
                .map(this::mapToPostResponse)
                .toList();
    }

    // ================= SUGGESTED USERS =================
    @Override
    public List<UserSearchResponse> getSuggestedUsers() {

        logger.info("Fetching suggested users");

        User currentUser = getCurrentUser();

        List<Follow> following =
                followRepository.findByFollower(currentUser);

        List<Long> excludedIds = new ArrayList<>();

        excludedIds.add(currentUser.getId());

        for (Follow follow : following) {

            excludedIds.add(
                    follow.getFollowing().getId()
            );
        }

        List<User> suggestedUsers =
                userRepository.findSuggestedUsers(excludedIds);

        logger.info(
                "Suggested users fetched: {}",
                suggestedUsers.size()
        );

        return suggestedUsers.stream()
                .map(user ->
                        mapToUserSearchResponse(
                                user,
                                currentUser
                        )
                )
                .toList();
    }

    // ================= PRIVATE METHODS =================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
                        ||
                        Objects.equals(
                                authentication.getPrincipal(),
                                "anonymousUser"
                        )
        ) {

            logger.error("User not authenticated");

            throw new ResourceNotFound(
                    "User not authenticated"
            );
        }

        return (User) authentication.getPrincipal();
    }

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
                .profileImageUrl(
                        user.getProfileImageUrl()
                )
                .followersCount(
                        user.getFollowersCount()
                )
                .following(following)
                .build();
    }

    private HashtagResponse mapToHashtagResponse(
            Hashtag hashtag
    ) {

        return HashtagResponse.builder()
                .id(hashtag.getId())
                .name(hashtag.getName())
                .postCount(
                        (long) hashtag.getPosts().size()
                )
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
                                                getCurrentUser().getId()
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