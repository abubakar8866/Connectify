package com.abubakar.connectify.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.dto.response.SavePostResponse;
import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.entity.SavedPost;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.exception.UnauthorizedException;
import com.abubakar.connectify.repository.PostRepository;
import com.abubakar.connectify.repository.SavedPostRepository;
import com.abubakar.connectify.service.SavedPostService;

import com.abubakar.connectify.util.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SavedPostServiceImpl implements SavedPostService {

    private static final Logger logger =
            LoggerFactory.getLogger(SavedPostServiceImpl.class);

    @Autowired
    private SavedPostRepository savedPostRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public SavePostResponse toggleSavePost(Long postId) {

        logger.info(
                "Toggle save post request | postId: {}",
                postId
        );

        User currentUser = this.authUtil.getCurrentUser();

        Post post = getPostById(postId);

        Optional<SavedPost> existingSave =
                savedPostRepository.findByUserAndPost(
                        currentUser,
                        post
                );

        // UNSAVE
        if (existingSave.isPresent()) {

            logger.info(
                    "Removing saved post | postId: {} | userId: {}",
                    postId,
                    currentUser.getId()
            );

            savedPostRepository.delete(existingSave.get());

            return SavePostResponse.builder()
                    .saved(false)
                    .postId(postId)
                    .build();
        }

        // SAVE
        SavedPost savedPost = SavedPost.builder()
                .user(currentUser)
                .post(post)
                .build();

        savedPostRepository.save(savedPost);

        logger.info(
                "Post saved successfully | postId: {} | userId: {}",
                postId,
                currentUser.getId()
        );

        return SavePostResponse.builder()
                .saved(true)
                .postId(postId)
                .build();
    }

    @Override
    public List<PostResponse> getSavedPosts() {

        logger.info("Fetching saved posts");

        User currentUser = this.authUtil.getCurrentUser();

        List<SavedPost> savedPosts =
                savedPostRepository
                        .findByUserOrderByCreatedAtDesc(
                                currentUser
                        );

        logger.info(
                "Total saved posts fetched: {}",
                savedPosts.size()
        );

        return savedPosts.stream()
                .map(savedPost ->
                        mapToPostResponse(
                                savedPost.getPost()
                        )
                )
                .toList();
    }

    // PRIVATE METHODS
    private Post getPostById(Long postId) {

        return postRepository.findById(postId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "Post not found with id: " + postId
                        )
                );
    }

    private PostResponse mapToPostResponse(Post post) {

        return PostResponse.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

}

