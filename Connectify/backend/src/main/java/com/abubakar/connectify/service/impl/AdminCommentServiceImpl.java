package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.CommentSearchRequest;
import com.abubakar.connectify.dto.response.AdminCommentResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.repository.CommentRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminCommentService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.specification.CommentSpecification;
import com.abubakar.connectify.util.*;

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
    private CommentAccessValidator commentAccessValidator;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Override
    public CursorPageResponse<AdminCommentResponse>
    getAllComments(

            CommentSearchRequest request,

            Long cursor,

            int size
    ) {

        logger.info(
                "Fetching comments for admin panel"
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.info(
                """
                Admin fetching comments
                | adminId: {}
                | keyword: {}
                | reportedOnly: {}
                | restoreRequested: {}
                | deleted: {}
                | cursor: {}
                | size: {}
                """,
                admin.getId(),
                request.getKeyword(),
                request.getReportedOnly(),
                request.getRestoreRequested(),
                request.getDeleted(),
                cursor,
                size
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Comment> comments =
                commentRepository.findAll(

                        CommentSpecification.searchComments(

                                request.getKeyword(),

                                request.getReportedOnly(),

                                request.getRestoreRequested(),

                                request.getDeleted(),

                                cursor
                        ),

                        pageable

                ).getContent();

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

    // ================= MODERATE COMMENT =================
    @Override
    public void moderateComment(
            Long commentId
    ) {

        logger.info(
                "Moderating comment | commentId: {}",
                commentId
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Comment comment =
                commentAccessValidator.getActiveComment(
                        commentId
                );

        comment.setDeleted(true);

        // RESET RESTORE REQUEST
        comment.setRestoreRequested(false);

        commentRepository.save(comment);

        notificationService.createNotification(

                comment.getUser().getId(),

                admin.getId(),

                "Your comment was removed by admin.",

                NotificationType.COMMENT,

                comment.getPost().getId(),

                comment.getId()
        );

        logger.info(
                "Comment moderated successfully | commentId: {}",
                commentId
        );
    }

    // ================= RESTORE COMMENT =================
    @Override
    public void acceptRestoreComment(
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

        Comment comment = commentAccessValidator.getComment(commentId);

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

        logger.info(
                "Comment restored successfully | commentId: {} | restoredByAdminId: {}",
                comment.getId(),
                admin.getId()
        );
    }

    // ================= REJECT RESTORE COMMENT =================
    @Override
    public void rejectRestoreComment(
            Long commentId
    ) {

        logger.info(
                "Rejecting comment restore | commentId: {}",
                commentId
        );

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        Comment comment =
                commentAccessValidator.getComment(
                        commentId
                );

        comment.setRestoreRequested(false);

        commentRepository.save(comment);

        notificationService.createNotification(

                comment.getUser().getId(),

                admin.getId(),

                "Your comment restore request was rejected.",

                NotificationType.COMMENT,

                comment.getPost().getId(),

                comment.getId()
        );

        logger.info(
                "Comment restore rejected successfully | commentId: {}",
                commentId
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

        Comment comment = commentAccessValidator.getComment(commentId);

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

