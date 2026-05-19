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

        Post post = getPostById(postId);

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

            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // NORMAL COMMENT NOTIFICATION
        if (request.getParentCommentId() == null) {

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
                "Comment added successfully | commentId: {} | userId: {}",
                savedComment.getId(),
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

        this.ownershipValidator.validate(
                    comment.getUser().getId(),
                    this.authUtil.getCurrentUser(),
                "You are not authorized to access this comment"
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

        ownershipValidator.validate(
                comment.getUser().getId(),
                authUtil.getCurrentUser(),
                "You are not authorized to access this comment"
        );

        comment.setDeleted(true);

        commentRepository.save(comment);

        logger.info(
                "Comment soft deleted successfully"
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

        ownershipValidator.validate(
                comment.getUser().getId(),
                authUtil.getCurrentUser(),
                "You are not authorized to access this comment"
        );

        if (!comment.getDeleted()) {

            throw new OperationFailException(
                    "Comment is not deleted"
            );
        }

        comment.setRestoreRequested(true);

        commentRepository.save(comment);

        logger.info(
                "Restore request submitted successfully"
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

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Comment> comments;

        if (cursor == null) {

            comments =
                    commentRepository
                            .findByPostIdAndParentCommentIsNullAndDeletedFalseOrderByIdDesc(
                                    postId,
                                    pageable
                            );

        } else {

            comments =
                    commentRepository
                            .findByPostIdAndParentCommentIsNullAndDeletedFalseAndIdLessThanOrderByIdDesc(
                                    postId,
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Comments fetched successfully | count: {}",
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

    private Post getPostById(Long postId) {

        logger.debug("Fetching post with id: {}", postId);

        return postRepository.findById(postId)
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

