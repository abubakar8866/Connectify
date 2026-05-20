package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Hashtag;
import com.abubakar.connectify.entity.Media;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminPostService;
import com.abubakar.connectify.service.FileService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.specification.PostSpecification;

import com.abubakar.connectify.util.AdminValidator;
import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.CursorPaginationUtil;
import com.abubakar.connectify.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminPostServiceImpl
        implements AdminPostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminPostServiceImpl.class
            );

    @Override
    public CursorPageResponse<AdminPostResponse> searchPosts(
            String keyword,
            String username,
            String hashtag,
            Boolean reportedOnly,
            Boolean restoreRequested,
            Long cursor,
            int size
    ) {

        User admin = authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.info(
                "Searching posts with cursor pagination"
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        Specification<Post> specification =
                Specification
                        .where(
                                PostSpecification.keyword(keyword)
                        )
                        .and(
                                PostSpecification.username(username)
                        )
                        .and(
                                PostSpecification.hashtag(hashtag)
                        )
                        .and(
                                PostSpecification.cursor(cursor)
                        ).and(
                                PostSpecification.restoreRequested(
                                        restoreRequested
                                )
                        );

        if (Boolean.TRUE.equals(reportedOnly)) {

            specification =
                    specification.and(
                            PostSpecification.reportedOnly()
                    );
        }

        List<Post> posts =
                postRepository.findAll(
                        specification,
                        pageable
                ).getContent();

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                this::mapToResponse
        );
    }

    @Override
    public void permanentlyDeletePost(
            Long postId
    ) {

        User admin = authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.info(
                "Admin permanently deleting post | postId: {}",
                postId
        );

        Post post =
                postRepository.findById(postId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Post not found with id: " + postId
                                )
                        );

        // DELETE MEDIA FILES FROM STORAGE
        for (Media media : post.getMediaList()) {

            fileService.deleteFile(
                    media.getUrl(),
                    "posts"
            );
        }

        // HARD DELETE FROM DATABASE
        postRepository.delete(post);

        notificationService.createNotification(
                post.getUser().getId(),
                this.authUtil.getCurrentUser().getId(),
                "Your post was permanently removed due to policy violation",
                NotificationType.POST_REMOVED,
                post.getId(),
                null
        );

        logger.info(
                "Post permanently deleted successfully"
        );
    }

    @Override
    public void restorePost(
            Long postId
    ) {

        User admin = authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.info(
                "Admin restoring post | postId: {}",
                postId
        );

        Post post =
                postRepository.findById(postId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "Post not found"
                                )
                        );

        post.setDeleted(false);

        post.setRestoreRequested(false);

        postRepository.save(post);

        notificationService.createNotification(
                post.getUser().getId(),
                this.authUtil.getCurrentUser().getId(),
                "Post restored successfully by Admin.",
                NotificationType.POST_REMOVED,
                post.getId(),
                null
        );

        logger.info(
                "Post restored successfully"
        );
    }

    // ================= MAP RESPONSE =================
    private AdminPostResponse mapToResponse(
            Post post
    ) {

        Long reportCount =
                reportRepository.countByPost(post);

        return AdminPostResponse.builder()

                .postId(post.getId())

                .caption(post.getCaption())

                .likeCount(post.getLikeCount())

                .commentCount(post.getCommentCount())

                .deleted(post.getDeleted())

                .restoreRequested(post.getRestoreRequested())

                .reportCount(reportCount)

                .username(
                        post.getUser().getUname()
                )

                .hashtags(
                        post.getHashtags()
                                .stream()
                                .map(Hashtag::getName)
                                .toList()
                )

                .mediaUrls(
                        post.getMediaList()
                                .stream()
                                .map(Media::getUrl)
                                .toList()
                )

                .createdAt(post.getCreatedAt())

                .build();
    }

}

