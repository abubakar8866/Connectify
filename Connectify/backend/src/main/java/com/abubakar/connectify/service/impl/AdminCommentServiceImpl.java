package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminCommentResponse;
import com.abubakar.connectify.entity.Comment;
import com.abubakar.connectify.entity.Report;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.CommentRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminCommentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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

    @Override
    public Page<AdminCommentResponse> getAllComments(
            int page,
            int size,
            String keyword
    ) {

        logger.info(
                "Fetching comments | page: {} | size: {} | keyword: {}",
                page,
                size,
                keyword
        );

        Pageable pageable =
                PageRequest.of(page, size);

        Page<Comment> comments;

        if (keyword != null && !keyword.isBlank()) {

            comments =
                    commentRepository
                            .findByContentContainingIgnoreCaseAndDeletedFalse(
                                    keyword,
                                    pageable
                            );

        } else {

            comments =
                    commentRepository
                            .findByDeletedFalse(pageable);
        }

        return comments.map(this::mapToResponse);
    }

    @Override
    public Page<AdminCommentResponse> getReportedComments(
            int page,
            int size
    ) {

        logger.info("Fetching reported comments");

        List<Report> reports =
                reportRepository.findByCommentIsNotNull();

        List<Comment> comments =
                reports.stream()
                        .map(Report::getComment)
                        .distinct()
                        .toList();

        Pageable pageable =
                PageRequest.of(page, size);

        int start =
                (int) pageable.getOffset();

        int end =
                Math.min(
                        start + pageable.getPageSize(),
                        comments.size()
                );

        List<AdminCommentResponse> responseList =
                comments.subList(start, end)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return new PageImpl<>(
                responseList,
                pageable,
                comments.size()
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
