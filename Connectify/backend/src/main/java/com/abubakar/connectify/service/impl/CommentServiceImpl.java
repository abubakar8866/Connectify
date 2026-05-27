package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.internal.CommentMappingData;
import com.abubakar.connectify.dto.request.CreateCommentRequest;
import com.abubakar.connectify.dto.response.CommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.CommentRepository;
import com.abubakar.connectify.repository.LikeRepository;
import com.abubakar.connectify.service.CommentService;

import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OwnershipValidator ownershipValidator;

    @Autowired
    private CommentAccessValidator commentAccessValidator;

    @Autowired
    private PostAccessValidator postAccessValidator;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger =
            LoggerFactory.getLogger(CommentServiceImpl.class);

    private static final int MAX_REPLY_DEPTH = 3;

    @Override
    public CommentResponse addComment(
            Long postId,
            CreateCommentRequest request
    ) {

        logger.info("Adding comment to post with id: {}", postId);

        User currentUser = this.authUtil.getCurrentUser();

        Post post = postAccessValidator.getActivePost(postId);

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .user(currentUser)
                .post(post)
                .build();

        // REPLY COMMENT
        if (request.getParentCommentId() != null) {

            logger.debug(
                    "Reply comment detected | parentCommentId: {}",
                    request.getParentCommentId()
            );

            Comment parentComment = commentAccessValidator
                    .getComment(request.getParentCommentId());

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

            int replyDepth =
                    calculateReplyDepth(parentComment);

            if (replyDepth >= MAX_REPLY_DEPTH) {

                logger.warn(
                        "Reply depth exceeded | parentCommentId: {} | depth: {}",
                        parentComment.getId(),
                        replyDepth
                );

                throw new OperationFailException(
                        "Maximum reply depth exceeded"
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
            CreateCommentRequest request
    ) {

        logger.info("Updating comment with id: {}", commentId);

        Comment comment = commentAccessValidator.getActiveComment(commentId);

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

        comment.setContent(request.getContent().trim());

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
                commentAccessValidator.getActiveComment(commentId);

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

        Comment comment = commentAccessValidator.getComment(commentId);

        if (comment.getPost().getDeleted()) {

            logger.warn(
                    "Cannot restore comment because parent post is deleted"
            );

            throw new OperationFailException(
                    "Cannot restore comment because parent post is deleted"
            );

        }

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
                    "Comment is not deleted"
            );

            throw new OperationFailException(
                    "Comment is not deleted"
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

        Post post =
                postAccessValidator.getActivePost(
                        postId
                );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Comment> parentComments;

        if (cursor == null) {

            parentComments =
                    commentRepository
                            .findByPostIdAndParentCommentIsNullAndDeletedFalseOrderByIdDesc(
                                    post.getId(),
                                    pageable
                            );

        } else {

            parentComments =
                    commentRepository
                            .findByPostIdAndParentCommentIsNullAndDeletedFalseAndIdLessThanOrderByIdDesc(
                                    post.getId(),
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Top-level comments fetched | count: {}",
                parentComments.size()
        );

        CommentMappingData mappingData =
                prepareCommentMappingData(
                        parentComments
                );

        return CursorPaginationUtil.buildResponse(
                parentComments,
                size,
                Comment::getId,
                comment -> mapToResponse(
                        comment,
                        mappingData,
                        0
                )
        );
    }

    // ================= PRIVATE METHODS =================

    private CommentResponse mapToResponse(
            Comment comment
    ) {

        CommentMappingData mappingData =
                prepareCommentMappingData(
                        List.of(comment)
                );

        return mapToResponse(
                comment,
                mappingData,
                0
        );
    }

    private CommentResponse mapToResponse(
            Comment comment,
            CommentMappingData mappingData,
            int depth
    ) {

        List<Comment> childReplies =
                mappingData.getRepliesMap()
                        .getOrDefault(
                                comment.getId(),
                                List.of()
                        );

        List<CommentResponse> replyResponses;

        if (depth >= MAX_REPLY_DEPTH) {

            replyResponses = List.of();

        } else {

            replyResponses =
                    childReplies.stream()

                            .map(reply ->
                                    mapToResponse(
                                            reply,
                                            mappingData,
                                            depth + 1
                                    )
                            )
                            .toList();
        }

        return CommentResponse.builder()

                .id(comment.getId())

                .content(comment.getContent().trim())

                .userId(comment.getUser().getId())

                .username(comment.getUser().getUname())

                .userProfileImage(
                        comment.getUser().getProfileImageUrl()
                )

                .likeCount(comment.getLikeCount())

                .liked(
                        mappingData.getLikedCommentIds()
                                .contains(comment.getId())
                )

                .createdAt(comment.getCreatedAt())

                .updatedAt(comment.getUpdatedAt())

                .replies(replyResponses)

                .replyCount(
                        (long) childReplies.size()
                )

                .build();
    }

    private List<Comment> fetchRepliesRecursively(
            List<Long> parentIds,
            int depth
    ) {

        if (
                parentIds.isEmpty()
                        ||
                        depth <= 0
        ) {

            return List.of();
        }

        List<Comment> replies =
                commentRepository.findRepliesByParentIds(
                        parentIds
                );

        List<Long> replyIds =
                replies.stream()
                        .map(Comment::getId)
                        .toList();

        List<Comment> nestedReplies =
                fetchRepliesRecursively(
                        replyIds,
                        depth - 1
                );

        List<Comment> allReplies =
                new ArrayList<>(replies);

        allReplies.addAll(
                nestedReplies
        );

        return allReplies;
    }

    private CommentMappingData prepareCommentMappingData(
            List<Comment> parentComments
    ) {

        User currentUser =
                authUtil.getCurrentUser();

        // PARENT IDS
        List<Long> parentIds =
                parentComments.stream()
                        .map(Comment::getId)
                        .toList();

        // FETCH ALL REPLIES
        List<Comment> allReplies =
                fetchRepliesRecursively(
                        parentIds,
                        MAX_REPLY_DEPTH
                );

        // GROUP REPLIES
        Map<Long, List<Comment>> repliesMap =
                allReplies.stream()
                        .collect(
                                Collectors.groupingBy(
                                        reply ->
                                                reply.getParentComment().getId()
                                )
                        );

        // ALL COMMENT IDS
        List<Long> allCommentIds =
                new ArrayList<>();

        allCommentIds.addAll(parentIds);

        allCommentIds.addAll(
                allReplies.stream()
                        .map(Comment::getId)
                        .toList()
        );

        // FETCH LIKED IDS
        Set<Long> likedCommentIds =
                likeRepository
                        .findLikedCommentIdsByUserAndCommentIds(
                                currentUser,
                                allCommentIds
                        );

        return new CommentMappingData(
                repliesMap,
                likedCommentIds
        );
    }

    private int calculateReplyDepth(
            Comment comment
    ) {

        int depth = 0;

        Comment current = comment;

        while (current.getParentComment() != null) {

            depth++;

            current = current.getParentComment();
        }

        return depth;
    }

}

