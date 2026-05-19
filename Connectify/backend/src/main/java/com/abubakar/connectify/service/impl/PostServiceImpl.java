package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.CreatePostRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.MediaType;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.exception.UnauthorizedException;
import com.abubakar.connectify.exception.UserNotAuthenticatedException;
import com.abubakar.connectify.repository.*;
import com.abubakar.connectify.service.FileService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.service.PostService;
import com.abubakar.connectify.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OwnershipValidator ownershipValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(PostServiceImpl.class);

    @Override
    public PostResponse createPost(
            CreatePostRequest request,
            List<MultipartFile> files)
    {

        logger.info("Creating new post");

        User user = this.authUtil.getCurrentUser();
        logger.info("Post creation requested by userId: {}", user.getId());

        Post post = new Post();

        post.setCaption(request.getCaption());
        post.setUser(user);
        post.setLikeCount(0L);
        post.setCommentCount(0L);

        // Extract Hashtags
        List<Hashtag> extractedTags =
                processHashtags(request.getCaption());

        post.setHashtags(extractedTags);

        logger.info(
                "Extracted hashtags: {}",
                extractedTags.stream()
                        .map(Hashtag::getName)
                        .toList()
        );

       // Save Post First
        post = postRepository.save(post);
        logger.info("Post saved successfully with id: {}", post.getId());

        // Upload Media
        List<Media> mediaList =
                uploadMedia(files, post);

        post.getMediaList().addAll(mediaList);
        logger.info("Uploaded {} media files for postId: {}",
                mediaList.size(),
                post.getId());

        post = postRepository.save(post);

        logger.info("Post creation completed successfully");
        return mapToResponse(post);
    }

    @Override
    public PostResponse updatePost(
            Long postId,
            CreatePostRequest request,
            List<MultipartFile> files
    ) {

        logger.info("Updating post with id: {}", postId);

        Post post = this.getPostById(postId);

        User currentUser = this.authUtil.getCurrentUser();

        // Ownership Check
        this.ownershipValidator.validate(
                post.getUser().getId(),
                currentUser,
                "You are not authorized to access this post"
        );
        logger.info("Ownership validation passed for userId: {}",
                currentUser.getId());

        // Update Caption
        post.setCaption(request.getCaption());

        // Re-extract hashtags
        List<Hashtag> extractedTags =
                processHashtags(request.getCaption());

        post.setHashtags(extractedTags);

        logger.info(
                "Extracted hashtags: {}",
                extractedTags.stream()
                        .map(Hashtag::getName)
                        .toList()
        );

        // Replace Media
        if (files != null && !files.isEmpty()) {

            // Delete old files from storage
            for (Media media : post.getMediaList()) {

                fileService.deleteFile(
                        media.getUrl(),
                        "posts"
                );
            }

            // Delete old media records
            mediaRepository.deleteAll(post.getMediaList());
            post.getMediaList().clear();
            logger.info("Deleting old media files for postId: {}", postId);

            // Upload new files
            List<Media> newMediaList =
                    uploadMedia(files, post);

            post.getMediaList().addAll(newMediaList);
            logger.info("Uploaded {} new media files",
                    newMediaList.size());
        }

        post = postRepository.save(post);
        logger.info("Post updated successfully with id: {}", postId);
        return mapToResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getSinglePost(
            Long postId
    ) {

        logger.info(
                "Fetching single post | postId: {}",
                postId
        );

        Post post = this.getPostById(postId);

        return mapToResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse>
    getFeed(
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching personalized feed"
        );

        User currentUser =
                authUtil.getCurrentUser();

        List<Follow> following =
                followRepository.findByFollower(
                        currentUser
                );

        List<User> users =
                new ArrayList<>();

        // OWN POSTS
        users.add(currentUser);

        // FOLLOWING USERS POSTS
        for (Follow follow : following) {

            users.add(
                    follow.getFollowing()
            );
        }

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Post> posts;

        // FIRST PAGE
        if (cursor == null) {

            posts =
                    postRepository
                            .findByUserInAndDeletedFalseAndUserDeletedFalseAndUserAccountStatusNotOrderByIdDesc(
                                    users,
                                    AccountStatus.BANNED,
                                    pageable
                            );

        }

        // NEXT PAGE
        else {

            posts =
                    postRepository
                            .findByUserInAndDeletedFalseAndUserDeletedFalseAndIdLessThanAndUserAccountStatusNotOrderByIdDesc(
                                    users,
                                    cursor,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                this::mapToResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PostResponse>
    getUserPosts(
            Long userId,
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching user posts | userId: {}",
                userId
        );

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "User not found"
                                )
                        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Post> posts;

        // FIRST PAGE
        if (cursor == null) {

            posts =
                    postRepository
                            .findByUserAndDeletedFalseAndUserDeletedFalseAndUserAccountStatusNotOrderByIdDesc(
                                    user,
                                    AccountStatus.BANNED,
                                    pageable
                            );

        }

        // NEXT PAGE
        else {

            posts =
                    postRepository
                            .findByUserAndDeletedFalseAndUserDeletedFalseAndIdLessThanAndUserAccountStatusNotOrderByIdDesc(
                                    user,
                                    cursor,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                this::mapToResponse
        );
    }

    @Override
    public void softDeletePost(
            Long postId
    ) {

        logger.info(
                "Soft deleting post | postId: {}",
                postId
        );

        Post post = getPostById(postId);

        User currentUser =
                authUtil.getCurrentUser();

        ownershipValidator.validate(
                post.getUser().getId(),
                currentUser,
                "You are not authorized to delete this post"
        );

        post.setDeleted(true);

        postRepository.save(post);

        notificationService.createNotification(
                post.getUser().getId(),
                currentUser.getId(),
                "Your post was removed due to policy violation",
                NotificationType.POST_REMOVED,
                post.getId(),
                null
        );

        logger.info(
                "Post soft deleted successfully"
        );
    }

    @Override
    public void requestRestorePost(
            Long postId
    ) {

        logger.info(
                "Restore request for postId: {}",
                postId
        );

        User currentUser =
                authUtil.getCurrentUser();

        Post post = this.getPostById(postId);

        // OWNERSHIP CHECK
        ownershipValidator.validate(
                post.getUser().getId(),
                currentUser,
                "You are not authorized"
        );

        if (!post.getDeleted()) {

            throw new OperationFailException(
                    "Post is not deleted."
            );
        }

        post.setRestoreRequested(true);

        postRepository.save(post);

        logger.info(
                "Restore request submitted"
        );
    }

    //private method
    private PostResponse mapToResponse(Post post) {

        User currentUser = authUtil.getCurrentUser();

        return PostResponse.builder()

                .id(post.getId())

                .caption(post.getCaption())

                .userId(post.getUser().getId())

                .username(post.getUser().getUname())

                .userProfileImage(
                        post.getUser().getProfileImageUrl()
                )

                .isVerified(
                        post.getUser().getIsVerified()
                )

                .likeCount(post.getLikeCount())

                .commentCount(post.getCommentCount())

                .liked(
                        isPostLikedByCurrentUser(post)
                )

                .mine(
                        currentUser.getId()
                                .equals(post.getUser().getId())
                )

                .createdAt(post.getCreatedAt())

                .updatedAt(post.getUpdatedAt())

                .mediaUrls(
                        post.getMediaList()
                                .stream()
                                .map(Media::getUrl)
                                .toList()
                )

                .hashtags(
                        post.getHashtags()
                                .stream()
                                .map(Hashtag::getName)
                                .toList()
                )

                .build();
    }

    private Post getPostById(Long postId){

        return postRepository
                .findByIdAndDeletedFalse(postId)
                .orElseThrow(() ->
                        new ResourceNotFound(
                                "Post not found with id: " + postId
                        )
                );
    }

    private List<Hashtag> processHashtags(String caption) {

        logger.debug("Processing hashtags from caption");

        Set<String> extractedTags =
                HashtagUtil.extractHashtags(caption);

        List<Hashtag> hashtags = new ArrayList<>();

        for (String tagName : extractedTags) {

            Optional<Hashtag> existing =
                    hashtagRepository.findByName(tagName);

            if (existing.isPresent()) {

                hashtags.add(existing.get());

            } else {

                Hashtag hashtag = new Hashtag();

                hashtag.setName(tagName);

                hashtags.add(
                        hashtagRepository.save(hashtag)
                );
            }
        }

        logger.debug("Found {} hashtags", extractedTags.size());
        return hashtags;
    }

    private List<Media> uploadMedia(
            List<MultipartFile> files,
            Post post
    ) {

        List<Media> mediaList = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return mediaList;
        }

        for (MultipartFile file : files) {

            String fileName =
                    fileService.uploadFile(
                            file,
                            post.getId(),
                            null,
                            "posts"
                    );

            Media media = new Media();

            media.setUrl(fileName);

            media.setPublicId(fileName);

            media.setPost(post);

            String type = file.getContentType();

            if (type != null && type.startsWith("video")) {

                media.setType(MediaType.VIDEO);

            } else {

                media.setType(MediaType.IMAGE);
            }

            mediaList.add(
                    mediaRepository.save(media)
            );
        }

        return mediaList;
    }

    private Boolean isPostLikedByCurrentUser(Post post) {

        User currentUser = this.authUtil.getCurrentUser();

        return likeRepository
                .findByUserAndPost(currentUser, post)
                .isPresent();
    }

}

