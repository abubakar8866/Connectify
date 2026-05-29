package com.abubakar.connectify.service.impl;

import java.util.Optional;

import com.abubakar.connectify.dto.response.LikeResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Like;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.CommentRepository;
import com.abubakar.connectify.repository.LikeRepository;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.service.LikeService;

import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.CommentAccessValidator;
import com.abubakar.connectify.util.PostAccessValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikeServiceImpl implements LikeService {

    private static final Logger logger =
            LoggerFactory.getLogger(LikeServiceImpl.class);

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private PostAccessValidator postAccessValidator;

    @Autowired
    private CommentAccessValidator commentAccessValidator;

    @Autowired
    private NotificationService notificationService;

    // TOGGLE POST LIKE
    @Override
    public LikeResponse togglePostLike(Long postId) {

        logger.info(
                "Toggle post like request | postId: {}",
                postId
        );

        User currentUser =
                authUtil.getCurrentUser();

        Post post =
                postAccessValidator.getActivePost(
                        postId
                );

        boolean alreadyLiked =
                likeRepository.existsByUserAndPost(
                        currentUser,
                        post
                );

        // UNLIKE
        if (alreadyLiked) {

            logger.debug(
                    """
                    Existing post like found
                    | postId: {}
                    | userId: {}
                    """,
                    postId,
                    currentUser.getId()
            );

            likeRepository.deleteByUserAndPost(
                    currentUser,
                    post
            );

            post.setLikeCount(
                    Math.max(
                            0,
                            post.getLikeCount() - 1
                    )
            );

            postRepository.save(post);

            logger.info(
                    """
                    Post unliked successfully
                    | postId: {}
                    | userId: {}
                    """,
                    postId,
                    currentUser.getId()
            );

            return LikeResponse.builder()
                    .liked(false)
                    .likeCount(post.getLikeCount())
                    .build();
        }

        try {

            Like like = Like.builder()
                    .user(currentUser)
                    .post(post)
                    .build();

            likeRepository.save(like);

            post.setLikeCount(
                    post.getLikeCount() + 1
            );

            postRepository.save(post);

            notificationService.createNotification(
                    post.getUser().getId(),
                    currentUser.getId(),
                    currentUser.getUname()
                            + " liked your post",
                    NotificationType.LIKE,
                    post.getId(),
                    null
            );

            logger.info(
                    """
                    Post liked successfully
                    | postId: {}
                    | userId: {}
                    """,
                    postId,
                    currentUser.getId()
            );

            return LikeResponse.builder()
                    .liked(true)
                    .likeCount(post.getLikeCount())
                    .build();

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    """
                    Duplicate post like prevented
                    | postId: {}
                    | userId: {}
                    """,
                    postId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "Post already liked"
            );
        }
    }

    // TOGGLE COMMENT LIKE
    @Override
    public LikeResponse toggleCommentLike(Long commentId) {

        logger.info(
                "Toggle comment like request | commentId: {}",
                commentId
        );

        User currentUser =
                this.authUtil.getCurrentUser();

        Comment comment =
                commentAccessValidator.getActiveComment(
                        commentId
                );

        boolean alreadyLiked =
                likeRepository.existsByUserAndComment(
                        currentUser,
                        comment
                );

        // UNLIKE
        if (alreadyLiked) {

            logger.debug(
                    """
                    Existing comment like found
                    | commentId: {}
                    | userId: {}
                    """,
                    commentId,
                    currentUser.getId()
            );

            Like existingLike =
                    likeRepository
                            .findByUserAndComment(
                                    currentUser,
                                    comment
                            )
                            .orElseThrow(() -> {

                                logger.warn(
                                        """
                                        Comment like not found during unlike
                                        | commentId: {}
                                        | userId: {}
                                        """,
                                        commentId,
                                        currentUser.getId()
                                );

                                return new ResourceNotFound(
                                        "Comment like not found"
                                );

                            });

            likeRepository.delete(existingLike);

            comment.setLikeCount(
                    Math.max(
                            0,
                            comment.getLikeCount() - 1
                    )
            );

            commentRepository.save(comment);

            logger.info(
                    """
                    Comment unliked successfully
                    | commentId: {}
                    | userId: {}
                    | likeCount: {}
                    """,
                    commentId,
                    currentUser.getId(),
                    comment.getLikeCount()
            );

            return LikeResponse.builder()
                    .liked(false)
                    .likeCount(comment.getLikeCount())
                    .build();
        }

        try {

            // LIKE
            Like like = Like.builder()
                    .user(currentUser)
                    .comment(comment)
                    .build();

            likeRepository.save(like);

            comment.setLikeCount(
                    comment.getLikeCount() + 1
            );

            commentRepository.save(comment);

            notificationService.createNotification(
                    comment.getUser().getId(),
                    currentUser.getId(),
                    currentUser.getUname()
                            + " liked your comment",
                    NotificationType.LIKE,
                    null,
                    comment.getId()
            );

            logger.info(
                    """
                    Comment liked successfully
                    | commentId: {}
                    | userId: {}
                    | likeCount: {}
                    """,
                    commentId,
                    currentUser.getId(),
                    comment.getLikeCount()
            );

            return LikeResponse.builder()
                    .liked(true)
                    .likeCount(comment.getLikeCount())
                    .build();

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    """
                    Duplicate comment like prevented
                    | commentId: {}
                    | userId: {}
                    """,
                    commentId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "Comment already liked"
            );
        }
    }

}

