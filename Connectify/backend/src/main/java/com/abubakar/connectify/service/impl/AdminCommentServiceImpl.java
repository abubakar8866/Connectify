package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminCommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.CommentRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminCommentService;

import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.CursorPaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCommentServiceImpl
        implements AdminCommentService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminCommentServiceImpl.class
            );

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public CursorPageResponse<AdminCommentResponse> getAllComments(
            Long cursor,
            int size,
            String keyword,
            Boolean reportedOnly
    ) {

        logger.info(
                "Fetching comments | cursor: {} | size: {} | keyword: {} | reportedOnly: {}",
                cursor,
                size,
                keyword,
                reportedOnly
        );

        Pageable pageable =
                PageRequest.of(0, size + 1);

        List<Comment> comments;

        // REPORTED COMMENTS
        if (Boolean.TRUE.equals(reportedOnly)) {

            comments = (cursor == null)

                    ? reportRepository.findReportedComments( pageable )

                    : reportRepository.findReportedCommentsByCursor(
                        cursor, pageable
                    );
        }

        // SEARCH COMMENTS
        else if (
                keyword != null
                        && !keyword.isBlank()
        ) {

            comments = (cursor == null)

                    ? commentRepository
                    .findByContentContainingIgnoreCaseAndDeletedFalseOrderByIdDesc(
                            keyword,
                            pageable
                    )

                    : commentRepository
                    .findByContentContainingIgnoreCaseAndDeletedFalseAndIdLessThanOrderByIdDesc(
                            keyword,
                            cursor,
                            pageable
                    );
        }

        // NORMAL COMMENTS
        else {

            comments = (cursor == null)

                    ? commentRepository
                    .findByDeletedFalseOrderByIdDesc(
                            pageable
                    )

                    : commentRepository
                    .findByDeletedFalseAndIdLessThanOrderByIdDesc(
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

    @Override
    public void deleteComment(
            Long commentId
    ) {

        logger.info(
                "Deleting abusive comment | commentId: {}",
                commentId
        );

        Comment comment =
                commentRepository.findById(commentId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Comment not found"
                                )
                        );

        comment.setDeleted(true);

        commentRepository.save(comment);

        User currentUser = this.authUtil.getCurrentUser();

        this.notificationService.createNotification(
                comment.getUser().getId(),
                currentUser.getId(),
                "Comment deleted by admin.",
                NotificationType.COMMENT,
                null,
                null
        );

        logger.info(
                "Comment deleted successfully | commentId: {}",
                commentId
        );
    }

    // ================= HELPER =================

    private AdminCommentResponse mapToResponse(
            Comment comment
    ) {

        Long reportsCount =
                reportRepository.countByComment(comment);

        return AdminCommentResponse.builder()

                .commentId(comment.getId())

                .content(comment.getContent())

                .userId(comment.getUser().getId())

                .username(comment.getUser().getUname())

                .postId(comment.getPost().getId())

                .likeCount(comment.getLikeCount())

                .deleted(comment.getDeleted())

                .reportsCount(reportsCount)

                .createdAt(comment.getCreatedAt())

                .build();
    }

}

