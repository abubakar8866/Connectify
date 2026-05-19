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
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.LikeService;

import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
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
    private UserRepository userRepository;

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

        // DELETED POST VALIDATION
        if (Boolean.TRUE.equals(post.getDeleted())) {

            throw new OperationFailException(
                    "Post is deleted"
            );
        }

        // BANNED USER VALIDATION
        if (post.getUser().getAccountStatus()== AccountStatus.BANNED) {

            throw new OperationFailException(
                    "Cannot interact with banned user's post"
            );
        }

        Optional<Like> existingLike =
                likeRepository.findByUserAndPost(currentUser, post);

        // UNLIKE
        if (existingLike.isPresent()) {

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
    }

    // TOGGLE COMMENT LIKE
    @Override
    public LikeResponse toggleCommentLike(Long commentId) {

        logger.info("Toggle comment like request | commentId: {}", commentId);

        User currentUser = this.authUtil.getCurrentUser();

        Comment comment = getCommentById(commentId);

        // DELETED COMMENT VALIDATION
        if (Boolean.TRUE.equals(comment.getDeleted())) {

            throw new OperationFailException(
                    "Comment is deleted"
            );
        }

        // BANNED USER VALIDATION
        if (
                comment.getUser().getAccountStatus()
                        == AccountStatus.BANNED
        ) {

            throw new OperationFailException(
                    "Cannot interact with banned user's comment"
            );
        }

        Optional<Like> existingLike =
                likeRepository.findByUserAndComment(currentUser, comment);

        // UNLIKE
        if (existingLike.isPresent()) {

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
    }

    // PRIVATE METHODS
    private Post getPostById(Long postId) {

        return postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "Post not found with id: " + postId
                        ));
    }

    private Comment getCommentById(Long commentId) {

        return commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "Comment not found with id: " + commentId
                        ));
    }

}

