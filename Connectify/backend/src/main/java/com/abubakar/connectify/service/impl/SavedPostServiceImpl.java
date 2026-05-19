package com.abubakar.connectify.service.impl;

import java.util.List;
import java.util.Optional;

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
    private AuthUtil authUtil;

    // ================= TOGGLE SAVE =================
    @Override
    public SavePostResponse toggleSavePost(Long postId) {

        logger.info(
                "Toggle save post request | postId: {}",
                postId
        );

        User currentUser = authUtil.getCurrentUser();

        Post post = getPostById(postId);

        Optional<SavedPost> existingSave =
                savedPostRepository.findByUserAndPost(
                        currentUser,
                        post
                );

        // UNSAVE
        if (existingSave.isPresent()) {

            logger.info(
                    "Removing saved post | postId: {} | userId: {}",
                    postId,
                    currentUser.getId()
            );

            savedPostRepository.delete(
                    existingSave.get()
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
    ) {

        logger.info(
                "Fetching saved posts | cursor: {} | size: {}",
                cursor,
                size
        );

        User currentUser =
                authUtil.getCurrentUser();

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<SavedPost> savedPosts;

        // FIRST PAGE
        if (cursor == null) {

            savedPosts =
                    savedPostRepository
                            .findByUserAndPostDeletedFalseAndPostUserAccountStatusNotOrderByIdDesc(
                                    currentUser,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        // NEXT PAGE
        else {

            savedPosts =
                    savedPostRepository
                            .findByUserAndPostDeletedFalseAndPostUserAccountStatusNotAndIdLessThanOrderByIdDesc(
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

        return CursorPaginationUtil.buildResponse(
                savedPosts,
                size,
                SavedPost::getId,
                this::mapToPostResponse
        );
    }

    // ================= PRIVATE METHODS =================
    private Post getPostById(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "Post not found with id: " + postId
                        )
                );

        // DELETED POST VALIDATION
        if (Boolean.TRUE.equals(post.getDeleted())) {

            throw new ResourceNotFound(
                    "Post not found"
            );
        }

        // BANNED USER VALIDATION
        if (post.getUser().getAccountStatus()
                == AccountStatus.BANNED) {

            throw new OperationFailException(
                    "This post is unavailable"
            );
        }

        return post;
    }

    private PostResponse mapToPostResponse(
            SavedPost savedPost
    ) {

        Post post = savedPost.getPost();

        User currentUser =
                authUtil.getCurrentUser();

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
                        false
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

                .updatedAt(
                        savedPost.getCreatedAt()
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

