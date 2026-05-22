package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminCommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.CommentRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminCommentService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.util.AdminValidator;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.CursorPaginationUtil;
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

    @Autowired
    private AdminValidator adminValidator;

    @Override
    public CursorPageResponse<AdminCommentResponse>
    getAllComments(
            Long cursor,
            int size,
            String keyword,
            Boolean reportedOnly,
            Boolean restoreRequested
    ) {

        logger.info(
                "Fetching comments for admin panel"
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.info(
                "Admin fetching comments | adminId: {} | cursor: {} | size: {} | keyword: {} | reportedOnly: {} | restoreRequested: {}",
                admin.getId(),
                cursor,
                size,
                keyword,
                reportedOnly,
                restoreRequested
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Comment> comments;

        // ================= RESTORE REQUESTED =================

        if (Boolean.TRUE.equals(restoreRequested)) {

            logger.debug(
                    "Fetching restore requested comments"
            );

            comments = (cursor == null)

                    ? commentRepository
                    .findByRestoreRequestedTrueOrderByIdDesc(
                            pageable
                    )

                    : commentRepository
                    .findByRestoreRequestedTrueAndIdLessThanOrderByIdDesc(
                            cursor,
                            pageable
                    );
        }

        // ================= REPORTED COMMENTS =================

        else if (Boolean.TRUE.equals(reportedOnly)) {

            logger.debug(
                    "Fetching reported comments"
            );

            comments = (cursor == null)

                    ? reportRepository.findReportedComments(
                    pageable
            )

                    : reportRepository
                    .findReportedCommentsByCursor(
                            cursor,
                            pageable
                    );
        }

        // ================= SEARCH =================

        else if (
                keyword != null
                        && !keyword.isBlank()
        ) {

            logger.debug(
                    "Searching comments | keyword: {}",
                    keyword
            );

            comments = (cursor == null)

                    ? commentRepository
                    .findByContentContainingIgnoreCaseOrderByIdDesc(
                            keyword,
                            pageable
                    )

                    : commentRepository
                    .findByContentContainingIgnoreCaseAndIdLessThanOrderByIdDesc(
                            keyword,
                            cursor,
                            pageable
                    );
        }

        // ================= ALL COMMENTS =================

        else {

            comments = (cursor == null)

                    ? commentRepository
                    .findAllByOrderByIdDesc(
                            pageable
                    )

                    : commentRepository
                    .findByIdLessThanOrderByIdDesc(
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

    // ================= RESTORE COMMENT =================
    @Override
    public void restoreComment(
            Long commentId
    ) {

        logger.info(
                "Restoring comment | commentId: {}",
                commentId
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.info(
                "Restore comment request received | adminId: {} | commentId: {}",
                admin.getId(),
                commentId
        );

        Comment comment =
                getCommentById(commentId);

        if (!comment.getDeleted()) {

            logger.warn(
                    "Restore failed | comment already active | commentId: {}",
                    commentId
            );

            throw new OperationFailException(
                    "Comment already restored."
            );
        }

        comment.setDeleted(false);

        comment.setRestoreRequested(false);

        commentRepository.save(comment);

        notificationService.createNotification(

                comment.getUser().getId(),

                admin.getId(),

                "Your comment has been restored by admin.",

                NotificationType.COMMENT,

                comment.getPost().getId(),

                comment.getId()
        );

        logger.debug(
                "Restore notification sent successfully | receiverId: {} | commentId: {}",
                comment.getUser().getId(),
                comment.getId()
        );

        logger.info(
                "Comment restored successfully | commentId: {} | restoredByAdminId: {}",
                comment.getId(),
                admin.getId()
        );
    }

    // ================= HARD DELETE COMMENT =================
    @Override
    public void permanentlyDeleteComment(
            Long commentId
    ) {

        logger.info(
                "Permanently deleting comment | commentId: {}",
                commentId
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.info(
                "Permanent delete request received | adminId: {} | commentId: {}",
                admin.getId(),
                commentId
        );

        Comment comment =
                getCommentById(commentId);

        notificationService.createNotification(

                comment.getUser().getId(),

                admin.getId(),

                "Your comment was permanently removed by admin.",

                NotificationType.COMMENT,

                null,

                null
        );

        logger.debug(
                "permanent delete notification sent successfully | receiverId: {} | commentId: {}",
                comment.getUser().getId(),
                comment.getId()
        );

        commentRepository.delete(comment);

        logger.info(
                "Comment permanently deleted successfully | commentId: {} | deletedByAdminId: {}",
                comment.getId(),
                admin.getId()
        );
    }

    // ================= HELPER =================
    private Comment getCommentById(
            Long commentId
    ) {

        logger.debug(
                "Fetching comment by id: {}",
                commentId
        );

        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    logger.error(
                            "Comment not found | commentId: {}",
                            commentId
                    );
                    return new ResourceNotFound(
                            "Comment not found"
                    );
                }

                );
    }

    private AdminCommentResponse mapToResponse(
            Comment comment
    ) {

        Long reportsCount =
                reportRepository.countByComment(
                        comment
                );

        return AdminCommentResponse.builder()

                .commentId(comment.getId())

                .content(comment.getContent())

                .userId(comment.getUser().getId())

                .username(comment.getUser().getUname())

                .postId(comment.getPost().getId())

                .likeCount(comment.getLikeCount())

                .deleted(comment.getDeleted())

                .restoreRequested(
                        comment.getRestoreRequested()
                )

                .reportsCount(reportsCount)

                .createdAt(comment.getCreatedAt())

                .build();
    }

}

