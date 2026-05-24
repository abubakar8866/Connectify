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
    private NotificationService notificationService;

    // TOGGLE POST LIKE
    @Override
    public LikeResponse togglePostLike(Long postId) {

        logger.info("Toggle post like request | postId: {}", postId);

        User currentUser = this.authUtil.getCurrentUser();

        Post post = getPostById(postId);

        Optional<Like> existingLike =
                likeRepository.findByUserAndPost(currentUser, post);

        // UNLIKE
        if (existingLike.isPresent()) {

            logger.debug(
                    "Existing post like found | postId: {} | userId: {}",
                    postId,
                    currentUser.getId()
            );

            likeRepository.delete(existingLike.get());

            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));

            postRepository.save(post);

            logger.info(
                    "Post unliked successfully | postId: {} | userId: {}",
                    postId,
                    currentUser.getId()
            );

            return LikeResponse.builder()
                    .liked(false)
                    .likeCount(post.getLikeCount())
                    .build();
        }

        try{

            // LIKE
            Like like = Like.builder()
                    .user(currentUser)
                    .post(post)
                    .build();

            likeRepository.save(like);

            post.setLikeCount(post.getLikeCount() + 1);

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
                    "Post liked successfully | postId: {} | userId: {}",
                    postId,
                    currentUser.getId()
            );

            return LikeResponse.builder()
                    .liked(true)
                    .likeCount(post.getLikeCount())
                    .build();

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    "Duplicate post like prevented | postId: {} | userId: {}",
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

        logger.info("Toggle comment like request | commentId: {}", commentId);

        User currentUser = this.authUtil.getCurrentUser();

        Comment comment = getCommentById(commentId);

        Optional<Like> existingLike =
                likeRepository.findByUserAndComment(currentUser, comment);

        // UNLIKE
        if (existingLike.isPresent()) {

            logger.debug(
                    "Existing comment like found | commentId: {} | userId: {}",
                    commentId,
                    currentUser.getId()
            );

            likeRepository.delete(existingLike.get());

            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));

            commentRepository.save(comment);

            logger.info(
                    "Comment unliked successfully | commentId: {} | userId: {}",
                    commentId,
                    currentUser.getId()
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

            comment.setLikeCount(comment.getLikeCount() + 1);

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
                    "Comment liked successfully | commentId: {} | userId: {}",
                    commentId,
                    currentUser.getId()
            );

            return LikeResponse.builder()
                    .liked(true)
                    .likeCount(comment.getLikeCount())
                    .build();

        } catch (DataIntegrityViolationException ex) {

            logger.warn(
                    "Duplicate comment like prevented | commentId: {} | userId: {}",
                    commentId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "Comment already liked"
            );

        }

    }

    // PRIVATE METHODS
    private Post getPostById(Long postId) {

        logger.debug(
                "Fetching post for like operation | postId: {}",
                postId
        );

        Post post =  postRepository.findById(postId)
                .orElseThrow(() -> {
                            logger.warn(
                                    "Post not found | postId: {}",
                                    postId
                            );
                            return new ResourceNotFound(
                                    "Post not found with id: " + postId
                            );
                        }
                );

        // DELETED POST VALIDATION
        if (Boolean.TRUE.equals(post.getDeleted())) {

            logger.warn(
                    "Like blocked because post is deleted | postId: {}",
                    postId
            );

            throw new OperationFailException(
                    "Post is deleted"
            );
        }

        // BANNED POST USER VALIDATION
        if (post.getUser().getAccountStatus()== AccountStatus.BANNED) {

            logger.warn(
                    "Like blocked because post owner is banned | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with banned user's post"
            );
        }

        // DELETED POST USER VALIDATION
        if (Boolean.TRUE.equals(post.getUser().getDeleted())) {

            logger.warn(
                    "Like blocked because post owner is deleted | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with deleted user's post"
            );
        }

        // INACTIVE USER POST VALIDATION
        if (Boolean.FALSE.equals(post.getUser().getIsActive())) {

            logger.warn(
                    "Like blocked because post owner is inactive | postId: {} | ownerId: {}",
                    postId,
                    post.getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with inactive user's post"
            );
        }

        logger.debug(
                "Post validation successful | postId: {}",
                postId
        );

        return  post;

    }

    private Comment getCommentById(Long commentId) {

        logger.debug(
                "Fetching comment for like operation | commentId: {}",
                commentId
        );

        Comment comment =  commentRepository.findById(commentId)
                .orElseThrow(() -> {
                            logger.warn(
                                    "Comment not found | commentId: {}",
                                    commentId
                            );
                            return new ResourceNotFound(
                                    "Comment not found with id: " + commentId
                            );
                        }
                );

        // DELETED COMMENT VALIDATION
        if (Boolean.TRUE.equals(comment.getDeleted())) {

            logger.warn(
                    "Like blocked because comment is deleted | commentId: {}",
                    commentId
            );

            throw new OperationFailException(
                    "Comment is deleted"
            );
        }

        // PARENT POST DELETED VALIDATION
        if (Boolean.TRUE.equals(comment.getPost().getDeleted())) {

            logger.warn(
                    "Like blocked because parent post is deleted | commentId: {} | postId: {}",
                    commentId,
                    comment.getPost().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with comment of deleted post"            );
        }

        // BANNED USER VALIDATION
        if (comment.getUser().getAccountStatus() == AccountStatus.BANNED) {

            logger.warn(
                    "Like blocked because comment owner is banned | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with banned user's comment"
            );
        }

        // DELETED USER COMMENT VALIDATION
        if (Boolean.TRUE.equals(comment.getUser().getDeleted())) {

            logger.warn(
                    "Like blocked because comment owner is deleted | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with deleted user's comment"
            );
        }

        // INACTIVE USER COMMENT VALIDATION
        if (Boolean.FALSE.equals(comment.getUser().getIsActive())) {

            logger.warn(
                    "Like blocked because comment owner is inactive | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with inactive user's comment"
            );
        }

        // PARENT POST OWNER BANNED
        if (comment.getPost().getUser().getAccountStatus() == AccountStatus.BANNED ) {

            logger.warn(
                    "Like blocked because parent post owner is banned | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getPost().getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with comment of banned user's post"
            );
        }

        // PARENT POST OWNER DELETED
        if (Boolean.TRUE.equals(comment.getPost().getUser().getDeleted())) {

            logger.warn(
                    "Like blocked because parent post owner is deleted | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getPost().getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with comment of deleted user's post"
            );
        }

        // PARENT POST OWNER INACTIVE
        if (Boolean.FALSE.equals(comment.getPost().getUser().getIsActive())) {

            logger.warn(
                    "Like blocked because parent post owner is inactive | commentId: {} | ownerId: {}",
                    commentId,
                    comment.getPost().getUser().getId()
            );

            throw new OperationFailException(
                    "Cannot interact with comment of inactive user's post"
            );
        }

        logger.debug(
                "Comment validation successful | commentId: {}",
                commentId
        );

        return  comment;

    }

}

