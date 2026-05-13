package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.CreateCommentRequest;
import com.abubakar.connectify.dto.response.CommentResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.exception.UnauthorizedException;
import com.abubakar.connectify.repository.CommentRepository;
import com.abubakar.connectify.repository.LikeRepository;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.CommentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private static final Logger logger =
            LoggerFactory.getLogger(CommentServiceImpl.class);

    @Override
    public CommentResponse addComment(
            Long postId,
            CreateCommentRequest request) {

        logger.info("Adding comment to post with id: {}", postId);

        User currentUser = getCurrentUser();

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

        ownershipCheck(comment);

        comment.setContent(request.getContent());

        Comment updatedComment = commentRepository.save(comment);

        logger.info(
                "Comment updated successfully | commentId: {}",
                updatedComment.getId()
        );

        return mapToResponse(updatedComment);
    }

    @Override
    public void deleteComment(Long commentId) {

        logger.info("Deleting comment with id: {}", commentId);

        Comment comment = getCommentById(commentId);

        ownershipCheck(comment);

        commentRepository.delete(comment);

        logger.info(
                "Comment deleted successfully | commentId: {}",
                commentId
        );
    }

    @Override
    public List<CommentResponse> getPostComments(Long postId) {

        logger.info("Fetching comments for postId: {}", postId);

        List<Comment> comments =
                commentRepository
                        .findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId);

        logger.info(
                "Total root comments fetched: {}",
                comments.size()
        );

        return comments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // PRIVATE METHODS
    private CommentResponse mapToResponse(Comment comment) {

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())

                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())

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

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        String email = authentication.getName();

        logger.debug(
                "Fetching current authenticated user | email: {}",
                email
        );

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {

                    logger.error(
                            "Authenticated user not found | email: {}",
                            email
                    );

                    return new ResourceNotFound(
                            "User not found"
                    );
                });
    }

    private void ownershipCheck(Comment comment) {

        User currentUser = getCurrentUser();

        if (!comment.getUser().getId().equals(currentUser.getId())) {

            logger.warn(
                    "Unauthorized comment access | commentOwnerId: {} | currentUserId: {}",
                    comment.getUser().getId(),
                    currentUser.getId()
            );

            throw new UnauthorizedException(
                    "You are not authorized to access this comment"
            );
        }

        logger.debug("Comment ownership validation successful");
    }

    private Boolean isCommentLikedByCurrentUser(Comment comment) {

        User currentUser = getCurrentUser();

        return likeRepository
                .findByUserAndComment(currentUser, comment)
                .isPresent();

    }

}

