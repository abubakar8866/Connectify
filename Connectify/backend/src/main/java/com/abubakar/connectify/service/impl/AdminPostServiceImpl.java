package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.PostSearchRequest;
import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Hashtag;
import com.abubakar.connectify.entity.Media;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.HashtagRepository;
import com.abubakar.connectify.repository.NotificationRepository;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminPostService;
import com.abubakar.connectify.service.FileService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.specification.PostSpecification;

import com.abubakar.connectify.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminPostServiceImpl
        implements AdminPostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AdminValidator adminValidator;

    @Autowired
    private PostAccessValidator postAccessValidator;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminPostServiceImpl.class
            );

    @Override
    public CursorPageResponse<AdminPostResponse> getPosts(
            PostSearchRequest request,
            Long cursor,
            int size
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.info(
                """
                Admin fetching posts
                | adminId: {}
                | keyword: {}
                | username: {}
                | hashtag: {}
                | reportedOnly: {}
                | restoreRequested: {}
                | deleted: {}
                | cursor: {}
                | size: {}
                """,
                admin.getId(),
                request.getKeyword(),
                request.getUsername(),
                request.getHashtag(),
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

        Specification<Post> specification =
                Specification
                        .where(
                                PostSpecification.keyword(
                                        request.getKeyword()
                                )
                        )
                        .and(
                                PostSpecification.username(
                                        request.getUsername()
                                )
                        )
                        .and(
                                PostSpecification.hashtag(
                                        request.getHashtag()
                                )
                        )
                        .and(
                                PostSpecification.restoreRequested(
                                        request.getRestoreRequested()
                                )
                        )
                        .and(
                                PostSpecification.deleted(
                                        request.getDeleted()
                                )
                        )
                        .and(
                                PostSpecification.cursor(
                                        cursor
                                )
                        );

        if (
                Boolean.TRUE.equals(
                        request.getReportedOnly()
                )
        ) {

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

        logger.info(
                "Posts fetched successfully | count: {}",
                posts.size()
        );

        List<Long> postIds =
                posts.stream()
                        .map(Post::getId)
                        .toList();

        Map<Long, Long> reportCountMap =
                reportRepository.getReportCounts(postIds)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]
                        ));

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                post -> mapToResponse(
                        post,
                        reportCountMap
                )
        );
    }

    @Override
    public void moderatePost(
            Long postId
    ) {

        User admin =
                authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.info(
                "Admin moderating post | adminId: {} | postId: {}",
                admin.getId(),
                postId
        );

        Post post =
                postAccessValidator.getPost(postId);

        if (post.getDeleted()) {

            logger.warn(
                    "Post already moderated"
            );

            throw new OperationFailException(
                    "Post already moderated"
            );
        }

        post.setDeleted(true);

        post.setRestoreRequested(false);

        // DECREMENT HASHTAG COUNTS
        for (Hashtag hashtag : post.getHashtags()) {

            hashtag.setPostCount(
                    hashtag.getPostCount() - 1
            );
        }

        hashtagRepository.saveAll(
                post.getHashtags()
        );

        postRepository.save(post);

        notificationService.createSystemNotification(

                post.getUser().getId(),

                admin.getId(),

                "Your post was removed by admin.",

                NotificationType.POST_REMOVED
        );

        logger.info(
                "Post moderated successfully | postId: {}",
                postId
        );
    }

    @Override
    public void approvePostRestore(
            Long postId
    ) {

        User admin = authUtil.getCurrentUser();
        adminValidator.validateAdmin(admin);

        logger.info(
                "Post restore requested by admin | adminId: {} | postId: {}",
                admin.getId(),
                postId
        );

        Post post = postAccessValidator.getPost(postId);

        if (!post.getDeleted()) {

            logger.warn(
                    "Post is already active"
            );

            throw new OperationFailException(
                    "Post is already active"
            );

        }

        if (!post.getRestoreRequested()) {

            logger.warn("No restore request found when calling restore post.");

            throw new OperationFailException(
                    "No restore request found"
            );

        }

        post.setDeleted(false);

        post.setRestoreRequested(false);

        // INCREMENT HASHTAG COUNTS
        for (Hashtag hashtag : post.getHashtags()) {

            hashtag.setPostCount(
                    hashtag.getPostCount() + 1
            );
        }
        hashtagRepository.saveAll(post.getHashtags());

        postRepository.save(post);

        notificationService.createSystemNotification(
                post.getUser().getId(),
                admin.getId(),
                "Post restored successfully by Admin.",
                NotificationType.POST_RESTORED
        );

        logger.info(
                "Post restored successfully | postId: {}",
                postId
        );
    }

    @Override
    public void rejectPostRestore(
            Long postId
    ) {

        User admin = authUtil.getCurrentUser();

        adminValidator.validateAdmin(admin);

        logger.info(
                "Restore rejection requested | adminId: {} | postId: {}",
                admin.getId(),
                postId
        );

        Post post = postAccessValidator.getPost(postId);

        if (!post.getDeleted()) {

            logger.warn(
                    "Post is already active then rejection failed"
            );

            throw new OperationFailException(
                    "Post is already active"
            );

        }

        if (!post.getRestoreRequested()) {

            logger.warn(
                    "Restore request not found"
            );

            throw new OperationFailException(
                    "Restore request not found"
            );
        }

        post.setRestoreRequested(false);

        postRepository.save(post);

        notificationService.createSystemNotification(
                post.getUser().getId(),
                admin.getId(),
                "Your post restore request was rejected by admin.",
                NotificationType.POST_RESTORE_REJECTED
        );

        logger.info(
                "Restore request rejected successfully | postId: {}",
                postId
        );
    }

    // ================= Helper Method =================
    private AdminPostResponse mapToResponse(
            Post post,
            Map<Long, Long> reportCountMap
    ) {

        return AdminPostResponse.builder()

                .postId(post.getId())

                .caption(post.getCaption())

                .likeCount(post.getLikeCount())

                .commentCount(post.getCommentCount())

                .deleted(post.getDeleted())

                .restoreRequested(post.getRestoreRequested())

                .reportCount(
                        reportCountMap.getOrDefault(
                                post.getId(),
                                0L
                        )
                )

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

