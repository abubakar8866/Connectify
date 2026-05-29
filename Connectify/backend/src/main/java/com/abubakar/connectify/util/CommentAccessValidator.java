package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.CommentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentAccessValidator {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserAccessValidator userAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(CommentAccessValidator.class);

    // USER APIs
    public Comment getActiveComment(Long commentId) {

        logger.debug(
                "Validating active comment access | commentId: {}",
                commentId
        );

        Comment comment = getComment(commentId);

        // COMMENT DELETED
        if (Boolean.TRUE.equals(comment.getDeleted())) {

            logger.warn(
                    "Comment access denied | deleted comment | commentId: {}",
                    commentId
            );

            throw new OperationFailException(
                    "Comment is deleted"
            );
        }

        // PARENT POST DELETED
        if (Boolean.TRUE.equals(comment.getPost().getDeleted())) {

            logger.warn(
                    """
                    Comment access denied
                    | parent post deleted
                    | commentId: {}
                    | postId: {}
                    """,
                    commentId,
                    comment.getPost().getId()
            );

            throw new OperationFailException(
                    "Parent post is deleted"
            );
        }

        // PARENT COMMENT VALIDATION
        Comment parentComment =
                comment.getParentComment();

        if (parentComment != null) {

            // PARENT COMMENT DELETED
            if (Boolean.TRUE.equals(parentComment.getDeleted())) {

                logger.warn(
                        """
                        Comment access denied
                        | parent comment deleted
                        | commentId: {}
                        | parentCommentId: {}
                        """,
                        commentId,
                        parentComment.getId()
                );

                throw new OperationFailException(
                        "Parent comment is deleted"
                );
            }

            // PARENT COMMENT OWNER VALIDATION
            userAccessValidator.validateActiveUser(
                    parentComment.getUser()
            );
        }

        // COMMENT OWNER VALIDATION
        userAccessValidator.validateActiveUser(
                comment.getUser()
        );

        // PARENT POST OWNER VALIDATION
        userAccessValidator.validateActiveUser(
                comment.getPost().getUser()
        );

        logger.debug(
                "Active comment validation successful | commentId: {}",
                commentId
        );

        return comment;
    }

    // ADMIN APIs
    public Comment getComment(Long commentId) {

        logger.debug(
                "Fetching comment | commentId: {}",
                commentId
        );

        return commentRepository.findById(commentId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Comment not found | commentId: {}",
                            commentId
                    );

                    return new ResourceNotFound(
                            "Comment not found with id: " + commentId
                    );
                });
    }

}

