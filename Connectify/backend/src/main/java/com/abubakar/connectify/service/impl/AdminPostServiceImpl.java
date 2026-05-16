package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.entity.Hashtag;
import com.abubakar.connectify.entity.Media;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminPostService;
import com.abubakar.connectify.specification.PostSpecification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminPostServiceImpl.class
            );

    @Override
    public List<AdminPostResponse> searchPosts(

            String keyword,

            String username,

            String hashtag,

            Boolean reportedOnly,

            Long cursor,

            int size
    ) {

        logger.info(
                "Searching posts with cursor pagination"
        );

        Pageable pageable =
                PageRequest.of(
                        0,
                        size,
                        Sort.by(Sort.Direction.DESC, "id")
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
                        );

        if (Boolean.TRUE.equals(reportedOnly)) {

            specification =
                    specification.and(
                            PostSpecification.reportedOnly()
                    );
        }

        return postRepository
                .findAll(specification, pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
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