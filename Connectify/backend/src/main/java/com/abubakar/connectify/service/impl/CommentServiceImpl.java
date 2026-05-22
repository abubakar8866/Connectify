package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.CreateCommentRequest;
import com.abubakar.connectify.dto.response.CommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.CommentRepository;
import com.abubakar.connectify.repository.LikeRepository;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.CommentService;

import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.CursorPaginationUtil;
import com.abubakar.connectify.util.OwnershipValidator;
import com.abubakar.connectify.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OwnershipValidator ownershipValidator;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger =
            LoggerFactory.getLogger(CommentServiceImpl.class);

    @Override
    public CommentResponse addComment(
            Long postId,
            CreateCommentRequest request) {

        logger.info("Adding comment to post with id: {}", postId);

        User currentUser = this.authUtil.getCurrentUser();

        Post post = getActivePostById(postId);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(currentUser)
                .post(post)
                .build();

        // REPLY COMMENT
        if (request.getParentCommentId() != null) {

            logger.debug(
                    "Reply comment detected | parentCommentId: {}",
                    request.getParentCommentId()
            );

            Comment parentComment =
                    getCommentById(request.getParentCommentId());

            if (!parentComment.getPost().getId().equals(postId)) {

                logger.warn(
                        "Parent comment does not belong to target post | parentCommentId: {} | targetPostId: {}",
                        parentComment.getId(),
                        postId
                );

                throw new OperationFailException(
                        "Parent comment does not belong to this post"
                );
            }

            if (parentComment.getDeleted()) {

                logger.warn(
                        "Reply attempt on deleted comment | parentCommentId: {}",
                        parentComment.getId()
                );

                throw new OperationFailException(
                        "Cannot reply to deleted comment"
                );
            }

            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // NORMAL COMMENT NOTIFICATION
        if (request.getParentCommentId() == null) {

            logger.debug(
                    "Creating post comment notification | receiverUserId: {} | commentId: {}",
                    post.getUser().getId(),
                    savedComment.getId()
            );

            notificationService.createNotification(
                    post.getUser().getId(),
                    currentUser.getId(),
                    currentUser.getUname() + " commented on your post",
                    NotificationType.COMMENT,
                    post.getId(),
                    savedComment.getId()
            );
        }

        // REPLY NOTIFICATION
        else {

            Comment parentComment =
                    savedComment.getParentComment();

            logger.debug(
                    "Creating reply notification | receiverUserId: {} | commentId: {}",
                    parentComment.getUser().getId(),
                    savedComment.getId()
            );

            notificationService.createNotification(
                    parentComment.getUser().getId(),
                    currentUser.getId(),
                    currentUser.getUname() + " replied to your comment",
                    NotificationType.REPLY,
                    post.getId(),
                    savedComment.getId()
            );
        }

        logger.info(
                "Comment created successfully | commentId: {} | postId: {} | userId: {}",
                savedComment.getId(),
                postId,
                currentUser.getId()
        );

        return mapToResponse(savedComment);
    }

    @Override
    public CommentResponse updateComment(
            Long commentId,
            CreateCommentRequest request) {

        logger.info("Updating comment with id: {}", commentId);

        Comment comment = getCommentById(commentId);

        if (comment.getDeleted()) {

            logger.warn(
                    "Update attempt on deleted comment | commentId: {}",
                    commentId
            );

            throw new OperationFailException(
                    "Deleted comment cannot be updated"
            );
        }

        this.ownershipValidator.validate(
                    comment.getUser().getId(),
                    this.authUtil.getCurrentUser(),
                "You are not authorized to access this comment"
                );
        logger.debug(
                "Ownership validation passed | commentId: {} | userId: {}",
                commentId,
                authUtil.getCurrentUser().getId()
        );

        comment.setContent(request.getContent());

        Comment updatedComment = commentRepository.save(comment);

        logger.info(
                "Comment updated successfully | commentId: {}",
                updatedComment.getId()
        );

        return mapToResponse(updatedComment);
    }

    @Override
    public void softDeleteComment(Long commentId) {

        logger.info(
                "Soft deleting comment with id: {}",
                commentId
        );

        Comment comment =
                getCommentById(commentId);

        if (comment.getDeleted()) {

            logger.warn(
                    "Comment already deleted | commentId: {}",
                    commentId
            );

            throw new OperationFailException(
                    "Comment already deleted"
            );
        }

        ownershipValidator.validate(
                comment.getUser().getId(),
                authUtil.getCurrentUser(),
                "You are not authorized to access this comment"
        );
        logger.debug(
                "Ownership validation passed for delete | commentId: {} | userId: {}",
                commentId,
                authUtil.getCurrentUser().getId()
        );

        comment.setDeleted(true);

        commentRepository.save(comment);

        logger.info(
                "Comment soft deleted successfully | commentId: {}",
                commentId
        );
    }

    @Override
    public void requestRestoreComment(Long commentId) {

        logger.info(
                "Restore request for commentId: {}",
                commentId
        );

        Comment comment =
                getCommentById(commentId);

        if (comment.getRestoreRequested()) {

            logger.warn(
                    "Duplicate restore request detected | commentId: {}",
                    commentId
            );

            throw new OperationFailException(
                    "Restore request already submitted"
            );
        }

        ownershipValidator.validate(
                comment.getUser().getId(),
                authUtil.getCurrentUser(),
                "You are not authorized to access this comment"
        );
        logger.debug(
                "Ownership validation passed for restore request | commentId: {} | userId: {}",
                commentId,
                authUtil.getCurrentUser().getId()
        );

        if (!comment.getDeleted()) {

            logger.warn(
                    "Comment is already deleted"
            );

            throw new OperationFailException(
                    "Comment is already deleted"
            );
        }

        comment.setRestoreRequested(true);

        commentRepository.save(comment);

        logger.info(
                "Comment restore request submitted successfully | commentId: {}",
                commentId
        );
    }

    // ================= CURSOR PAGINATION =================
    @Override
    public CursorPageResponse<CommentResponse> getPostComments(
            Long postId,
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching comments | postId: {} | cursor: {} | size: {}",
                postId,
                cursor,
                size
        );

        Post post = this.getActivePostById(postId);

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Comment> comments;

        if (cursor == null) {

            logger.debug(
                    "Fetching first page comments | postId: {}",
                    postId
            );

            comments =
                    commentRepository
                            .findByPostIdAndParentCommentIsNullAndDeletedFalseOrderByIdDesc(
                                    post.getId(),
                                    pageable
                            );

        } else {

            logger.debug(
                    "Fetching paginated comments | postId: {} | cursor: {}",
                    postId,
                    cursor
            );

            comments =
                    commentRepository
                            .findByPostIdAndParentCommentIsNullAndDeletedFalseAndIdLessThanOrderByIdDesc(
                                    post.getId(),
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Comments fetched successfully | postId: {} | fetchedCount: {}",
                postId,
                comments.size()
        );

        return CursorPaginationUtil.buildResponse(
                comments,
                size,
                Comment::getId,
                this::mapToResponse
        );
    }

    // PRIVATE METHODS
    private CommentResponse mapToResponse(Comment comment) {

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())

                .userId(comment.getUser().getId())
                .username(comment.getUser().getUname())
                .userProfileImage(
                        comment.getUser().getProfileImageUrl()
                )

                .likeCount(comment.getLikeCount())
                .liked(isCommentLikedByCurrentUser(comment))

                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())

                .replies(
                        comment.getReplies()
                                .stream()
                                .map(this::mapToResponse)
                                .toList()
                )
                .build();
    }

    private Comment getCommentById(Long commentId) {

        logger.debug("Fetching comment with id: {}", commentId);

        return commentRepository.findById(commentId)
                .orElseThrow(() -> {

                    logger.error(
                            "Comment not found with id: {}",
                            commentId
                    );

                    return new ResourceNotFound(
                            "Comment not found with id: " + commentId
                    );
                });
    }

    private Post getActivePostById(Long postId) {

        logger.debug("Fetching post with id: {}", postId);

        return postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> {

                    logger.error(
                            "Post not found with id: {}",
                            postId
                    );

                    return new ResourceNotFound(
                            "Post not found with id: " + postId
                    );
                });
    }

    private Boolean isCommentLikedByCurrentUser(Comment comment) {

        User currentUser = this.authUtil.getCurrentUser();

        return likeRepository
                .findByUserAndComment(currentUser, comment)
                .isPresent();

    }

}

