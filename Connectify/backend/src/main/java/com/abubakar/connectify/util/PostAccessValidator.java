package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.Post;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostAccessValidator {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccessValidator userAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(PostAccessValidator.class);

    // USER APIs
    public Post getActivePost(Long postId) {

        logger.debug(
                "Validating active post access | postId: {}",
                postId
        );

        Post post = getPost(postId);

        if (Boolean.TRUE.equals(post.getDeleted())) {

            logger.warn(
                    "Post access denied | deleted post | postId: {}",
                    postId
            );

            throw new OperationFailException(
                    "Post is deleted"
            );
        }

        //Validate Post Owner
        userAccessValidator.validateActiveUser(post.getUser());

        logger.debug(
                "Active post validation successful | postId: {}",
                postId
        );

        return post;
    }

    // ADMIN APIs
    public Post getPost(Long postId) {

        logger.debug(
                "Fetching post | postId: {}",
                postId
        );

        return postRepository.findById(postId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Post not found | postId: {}",
                            postId
                    );

                    return new ResourceNotFound(
                            "Post not found with id: " + postId
                    );
                });
    }

}

