package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.response.AdminPostResponse;
import com.abubakar.connectify.entity.Hashtag;
import com.abubakar.connectify.entity.Media;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.ReportRepository;
import com.abubakar.connectify.service.AdminPostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminPostServiceImpl
        implements AdminPostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReportRepository reportRepository;

    private static final Logger logger = LoggerFactory.getLogger(AdminPostServiceImpl.class);

    @Override
    public Page<AdminPostResponse> getAllPosts( int page, int size ) {

        logger.info(
                "Fetching all posts"
        );

        Pageable pageable =
                PageRequest.of(page, size);

        return postRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<AdminPostResponse> getReportedPosts( int page, int size ) {

        logger.info(
                "Fetching reported posts"
        );

        Pageable pageable =
                PageRequest.of(page, size);

        return reportRepository
                .findReportedPosts(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<AdminPostResponse>
    searchPostsByKeyword( String keyword, int page, int size ) {

        logger.info(
                "Searching posts by keyword: {}",
                keyword
        );

        Pageable pageable =
                PageRequest.of(page, size);

        return postRepository
                .findByCaptionContainingIgnoreCase(
                        keyword,
                        pageable
                )
                .map(this::mapToResponse);
    }

    @Override
    public Page<AdminPostResponse>
    searchPostsByUsername( String username, int page, int size ) {

        logger.info(
                "Searching posts by username: {}",
                username
        );

        Pageable pageable =
                PageRequest.of(page, size);

        return postRepository
                .searchByUsername(
                        username,
                        pageable
                )
                .map(this::mapToResponse);
    }

    @Override
    public Page<AdminPostResponse>
    searchPostsByHashtag( String hashtag, int page, int size ) {

        logger.info(
                "Searching posts by hashtag: {}",
                hashtag
        );

        Pageable pageable =
                PageRequest.of(page, size);

        return postRepository
                .searchByHashtag(
                        hashtag,
                        pageable
                )
                .map(this::mapToResponse);
    }

    // ================= MAP RESPONSE =================
    private AdminPostResponse mapToResponse( Post post ) {

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

