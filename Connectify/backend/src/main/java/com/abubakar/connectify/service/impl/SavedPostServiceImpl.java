package com.abubakar.connectify.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.SavePostResponse;
import com.abubakar.connectify.entity.Hashtag;
import com.abubakar.connectify.entity.Media;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.SavedPost;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.LikeRepository;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.SavedPostRepository;
import com.abubakar.connectify.service.SavedPostService;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.CursorPaginationUtil;
import com.abubakar.connectify.util.PaginationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SavedPostServiceImpl implements SavedPostService {

    private static final Logger logger =
            LoggerFactory.getLogger(SavedPostServiceImpl.class);

    @Autowired
    private SavedPostRepository savedPostRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private AuthUtil authUtil;

    // ================= TOGGLE SAVE =================
    @Override
    public SavePostResponse toggleSavePost(Long postId) {

        logger.info(
                "Toggle save post started | postId: {}",
                postId
        );

        User currentUser = authUtil.getCurrentUser();

        logger.debug(
                "Authenticated user fetched | userId: {}",
                currentUser.getId()
        );

        Post post = validateAndGetPost(postId);

        Optional<SavedPost> existingSave =
                savedPostRepository.findByUserAndPost(
                        currentUser,
                        post
                );

        // UNSAVED
        logger.debug(
                "Checking existing saved post | userId: {} | postId: {}",
                currentUser.getId(),
                postId
        );
        if (existingSave.isPresent()) {

            logger.info(
                    "Removing saved post | postId: {} | userId: {}",
                    postId,
                    currentUser.getId()
            );

            savedPostRepository.delete(
                    existingSave.get()
            );

            logger.info(
                    "Post unsaved successfully | postId: {} | userId: {}",
                    postId,
                    currentUser.getId()
            );

            return SavePostResponse.builder()
                    .saved(false)
                    .postId(postId)
                    .build();
        }

        // SAVE
        SavedPost savedPost = SavedPost.builder()
                .user(currentUser)
                .post(post)
                .build();

        savedPostRepository.save(savedPost);

        logger.info(
                "Post saved successfully | postId: {} | userId: {}",
                postId,
                currentUser.getId()
        );

        return SavePostResponse.builder()
                .saved(true)
                .postId(postId)
                .build();
    }

    // ================= GET SAVED POSTS =================
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse> getSavedPosts(
            Long cursor,
            int size
    )   {

        logger.info(
                "Fetching saved posts | cursor: {} | size: {}",
                cursor,
                size
        );

        User currentUser =
                authUtil.getCurrentUser();

        logger.debug(
                "Authenticated user fetched inside getSaved method | userId: {}",
                currentUser.getId()
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<SavedPost> savedPosts;

        // FIRST PAGE
        if (cursor == null) {

            logger.debug(
                    "Fetching first page of saved posts | userId: {}",
                    currentUser.getId()
            );

            savedPosts =
                    savedPostRepository
                            .findByUserAndPostDeletedFalseAndPostUserDeletedFalseAndPostUserIsActiveTrueAndPostUserAccountStatusNotOrderByIdDesc(
                                    currentUser,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        // NEXT PAGE
        else {

            logger.debug(
                    "Fetching saved posts with cursor | userId: {} | cursor: {}",
                    currentUser.getId(),
                    cursor
            );

            savedPosts =
                    savedPostRepository
                            .findByUserAndPostDeletedFalseAndPostUserDeletedFalseAndPostUserIsActiveTrueAndPostUserAccountStatusNotAndIdLessThanOrderByIdDesc(
                                    currentUser,
                                    AccountStatus.BANNED,
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Saved posts fetched successfully | count: {}",
                savedPosts.size()
        );

        List<Long> postIds =
                savedPosts.stream()
                        .map(savedPost ->
                                savedPost.getPost().getId()
                        )
                        .toList();

        logger.debug(
                "Collected post IDs from saved posts | count: {}",
                postIds.size()
        );

        Set<Long> likedPostIds;

        logger.debug(
                "Fetching liked post IDs | userId: {} | postCount: {}",
                currentUser.getId(),
                postIds.size()
        );

        if (postIds.isEmpty()) {

            likedPostIds = Set.of();
        }

        else {

            likedPostIds =
                    likeRepository.findLikedPostIdsByUserAndPostIds(
                            currentUser,
                            postIds
                    );
        }

        logger.info(
                "Saved posts response generated successfully | userId: {} | responseCount: {}",
                currentUser.getId(),
                savedPosts.size()
        );

        return CursorPaginationUtil.buildResponse(
                savedPosts,
                size,
                SavedPost::getId,
                savedPost -> mapToPostResponse(
                        savedPost,
                        currentUser,
                        likedPostIds
                )
        );
    }

    // ================= PRIVATE METHODS =================
    private Post validateAndGetPost(Long postId) {

        logger.debug(
                "Validating post for save operation | postId: {}",
                postId
        );

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Save post failed | post not found | postId: {}",
                            postId
                    );

                    return new ResourceNotFound(
                            "Post not found with id = "+postId
                    );
                });

        logger.debug(
                "Post found successfully | postId: {} | ownerId: {}",
                post.getId(),
                post.getUser().getId()
        );

        // DELETED POST VALIDATION
        if (Boolean.TRUE.equals(post.getDeleted())) {

            logger.warn(
                    "Save post failed | post is deleted | postId: {}",
                    postId
            );

            throw new OperationFailException(
                    "Save post already deleted."
            );
        }

        // BANNED OWNER VALIDATION
        if (post.getUser().getAccountStatus()
                == AccountStatus.BANNED) {

            logger.warn(
                    "Save post failed | owner is banned | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "This Save post owner is banned"
            );
        }

        // DELETED OWNER VALIDATION
        if (Boolean.TRUE.equals(post.getUser().getDeleted())) {

            logger.warn(
                    "Save post failed | owner is deleted | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "This Save post owner is deleted"
            );
        }

        // ACTIVE OWNER VALIDATION
        if (Boolean.FALSE.equals(post.getUser().getIsActive())) {

            logger.warn(
                    "Save post failed | owner is inactive | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "This Save post owner is not active"
            );
        }

        logger.debug(
                "Post validation successful | postId: {}",
                postId
        );

        return post;
    }

    private PostResponse mapToPostResponse(
            SavedPost savedPost,
            User currentUser,
            Set<Long> likedPostIds
    ) {

        Post post = savedPost.getPost();

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
                        currentUser.getId()
                                .equals(
                                        post.getUser().getId()
                                )
                )

                .createdAt(
                        post.getCreatedAt()
                )

                .savedAt(
                        savedPost.getCreatedAt()
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

