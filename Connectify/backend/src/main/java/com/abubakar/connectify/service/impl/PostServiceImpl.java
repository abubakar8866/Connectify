package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.CreatePostRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.PostCountResponse;
import com.abubakar.connectify.dto.response.PostResponse;
import com.abubakar.connectify.entity.*;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.enums.MediaType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.*;
import com.abubakar.connectify.service.FileService;
import com.abubakar.connectify.service.PostService;
import com.abubakar.connectify.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
    private AuthUtil authUtil;

    @Autowired
    private OwnershipValidator ownershipValidator;

    @Autowired
    private UserAccessValidator userAccessValidator;

    @Autowired
    private PostAccessValidator postAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(PostServiceImpl.class);

    @Override
    public PostResponse createPost(
            CreatePostRequest request,
            List<MultipartFile> files
    ) {

        logger.info("Creating new post");

        User user = authUtil.getCurrentUser();

        List<String> uploadedFiles =
                new ArrayList<>();

        try {

            Post post = new Post();

            post.setCaption(request.getCaption());
            post.setUser(user);
            post.setLikeCount(0L);
            post.setCommentCount(0L);

            // HASHTAGS
            List<Hashtag> extractedTags =
                    processHashtags(request.getCaption());

            post.setHashtags(extractedTags);

            // SAVE POST
            post = postRepository.save(post);

            // MEDIA
            List<Media> mediaList =
                    uploadMedia(
                            files,
                            post,
                            uploadedFiles
                    );

            post.getMediaList().addAll(mediaList);

            post = postRepository.save(post);

            logger.info(
                    "Post created successfully | postId: {}",
                    post.getId()
            );

            Set<Long> likedPostIds =
                    buildSingleLikedPostSet(
                            post,
                            user
                    );

            return mapToResponse(
                    post,
                    likedPostIds,
                    user
            );

        } catch (Exception e) {

            logger.error(
                    "Post creation failed | cleaning uploaded files"
            );

            for (String fileName : uploadedFiles) {

                fileService.deleteFile(
                        fileName,
                        "posts"
                );
            }

            throw e;
        }
    }

    @Override
    public PostResponse updatePost(
            Long postId,
            CreatePostRequest request,
            List<MultipartFile> files
    ) {

        logger.info(
                "Updating post with id: {}",
                postId
        );

        Post post =
                postAccessValidator.getPost(postId);

        User currentUser =
                authUtil.getCurrentUser();

        // TRACK NEWLY UPLOADED FILES
        List<String> uploadedFiles =
                new ArrayList<>();

        // BACKUP OLD FILES
        List<String> oldFiles =
                post.getMediaList()
                        .stream()
                        .map(Media::getUrl)
                        .toList();

        try {

            // OWNERSHIP VALIDATION
            ownershipValidator.validate(
                    post.getUser().getId(),
                    currentUser,
                    "You are not authorized to access this post"
            );

            logger.info(
                    "Ownership validation passed | userId: {}",
                    currentUser.getId()
            );

            // UPDATE CAPTION
            post.setCaption(
                    request.getCaption()
            );

            // REMOVE OLD HASHTAG COUNTS
            List<Hashtag> oldTags =
                    post.getHashtags();

            for (Hashtag hashtag : oldTags) {

                hashtag.setPostCount(
                        Math.max(
                                0,
                                hashtag.getPostCount() - 1
                        )
                );

                hashtagRepository.save(
                        hashtag
                );
            }

            // NEW HASHTAGS
            List<Hashtag> extractedTags =
                    processHashtags(
                            request.getCaption()
                    );

            post.setHashtags(
                    extractedTags
            );

            logger.info(
                    "Updated hashtags | postId: {} | hashtags: {}",
                    postId,
                    extractedTags.stream()
                            .map(Hashtag::getName)
                            .toList()
            );

            // REPLACE MEDIA
            if (files != null && !files.isEmpty()) {

                logger.info(
                        "Replacing media files | postId: {}",
                        postId
                );

                // DELETE OLD MEDIA RECORDS
                mediaRepository.deleteAll(
                        post.getMediaList()
                );

                post.getMediaList().clear();

                // UPLOAD NEW FILES
                List<Media> newMediaList =
                        uploadMedia(
                                files,
                                post,
                                uploadedFiles
                        );

                post.getMediaList()
                        .addAll(newMediaList);

                logger.info(
                        "Uploaded {} new files | postId: {}",
                        newMediaList.size(),
                        postId
                );
            }

            post = postRepository.save(post);

            // DELETE OLD FILES ONLY AFTER SUCCESS
            if (files != null && !files.isEmpty()) {

                for (String oldFile : oldFiles) {

                    fileService.deleteFile(
                            oldFile,
                            "posts"
                    );
                }
            }

            logger.info(
                    "Post updated successfully | postId: {}",
                    postId
            );

            Set<Long> likedPostIds =
                    buildSingleLikedPostSet(
                            post,
                            currentUser
                    );

            return mapToResponse(
                    post,
                    likedPostIds,
                    currentUser
            );

        } catch (Exception e) {

            logger.error(
                    "Post update failed | cleaning uploaded files | postId: {}",
                    postId,
                    e
            );

            // CLEANUP NEWLY UPLOADED FILES
            for (String fileName : uploadedFiles) {

                fileService.deleteFile(
                        fileName,
                        "posts"
                );
            }

            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getSinglePost(
            Long postId
    ) {

        User user = this.authUtil.getCurrentUser();

        logger.info(
                "Fetching single post | postId: {}",
                postId
        );

        Post post = postAccessValidator.getActivePost(postId);

        logger.debug(
                "Post fetched successfully | postId: {}",
                postId
        );

        Set<Long> likedPostIds = this.buildSingleLikedPostSet(post,user);

        return mapToResponse(
                post,
                likedPostIds,
                user
        );
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

        logger.info(
                "Fetching personalized feed | userId: {} | cursor: {} | size: {}",
                currentUser.getId(),
                cursor,
                size
        );

        List<Follow> following =
                followRepository.findByFollower(
                        currentUser
                );

        logger.debug(
                "Collected following users | userId: {} | followingCount: {}",
                currentUser.getId(),
                following.size()
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

            logger.debug(
                    "Fetching first page of feed"
            );

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

            logger.debug(
                    "Fetching next page of feed | cursor: {}",
                    cursor
            );

            posts =
                    postRepository
                            .findByUserInAndDeletedFalseAndUserDeletedFalseAndIdLessThanAndUserAccountStatusNotOrderByIdDesc(
                                    users,
                                    cursor,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        logger.info(
                "Feed fetched successfully | resultCount: {}",
                posts.size()
        );

        List<Long> postIds =
                posts.stream()
                        .map(Post::getId)
                        .toList();

        Set<Long> likedPostIds =
                postIds.isEmpty()
                        ? Set.of()
                        : likeRepository.findLikedPostIdsByUserAndPostIds(
                        currentUser,
                        postIds
                );

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                post -> mapToResponse(
                        post,
                        likedPostIds,
                        currentUser
                )
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
                "Fetching user posts | targetUserId: {} | cursor: {} | size: {}",
                userId,
                cursor,
                size
        );

        // TARGET USER (PROFILE OWNER)
        User targetUser =
                this.userAccessValidator.getValidUser(
                        userId
                );

        // CURRENT LOGGED-IN USER (VIEWER)
        User currentUser =
                this.authUtil.getCurrentUser();

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Post> posts;

        // FIRST PAGE
        if (cursor == null) {

            logger.debug(
                    "Fetching first page of user posts"
            );

            posts =
                    postRepository
                            .findByUserAndDeletedFalseAndUserDeletedFalseAndUserAccountStatusNotOrderByIdDesc(
                                    targetUser,
                                    AccountStatus.BANNED,
                                    pageable
                            );

        }

        // NEXT PAGE
        else {

            logger.debug(
                    "Fetching next page of user posts | cursor: {}",
                    cursor
            );

            posts =
                    postRepository
                            .findByUserAndDeletedFalseAndUserDeletedFalseAndIdLessThanAndUserAccountStatusNotOrderByIdDesc(
                                    targetUser,
                                    cursor,
                                    AccountStatus.BANNED,
                                    pageable
                            );
        }

        logger.info(
                "User posts fetched successfully | targetUserId: {} | resultCount: {}",
                userId,
                posts.size()
        );

        List<Long> postIds =
                posts.stream()
                        .map(Post::getId)
                        .toList();

        // CHECK WHICH POSTS ARE LIKED BY CURRENT VIEWER
        Set<Long> likedPostIds =
                postIds.isEmpty()
                        ? Set.of()
                        : likeRepository.findLikedPostIdsByUserAndPostIds(
                        currentUser,
                        postIds
                );

        return CursorPaginationUtil.buildResponse(
                posts,
                size,
                Post::getId,
                post -> mapToResponse(
                        post,
                        likedPostIds,
                        currentUser
                )
        );
    }

    // ================= GET POST COUNT =================
    @Override
    @Transactional(readOnly = true)
    public PostCountResponse getPostCount(
            Long userId
    ) {

        logger.info(
                "Fetching post count | requestedUserId: {}",
                userId
        );

        User targetUser;

        // CURRENT USER
        if (userId == null) {

            targetUser =
                    authUtil.getCurrentUser();

            logger.info(
                    "Fetching post count for current user | userId: {}",
                    targetUser.getId()
            );

        }

        // TARGET USER
        else {

            targetUser =
                    userAccessValidator.getValidUser(
                            userId
                    );

            logger.info(
                    "Fetching post count for target user | userId: {}",
                    targetUser.getId()
            );
        }

        Long postCount =
                postRepository.countByUserAndDeletedFalse(
                        targetUser
                );

        logger.info(
                """
                Post count fetched successfully
                | userId: {}
                | postCount: {}
                """,
                targetUser.getId(),
                postCount
        );

        return PostCountResponse.builder()
                .userId(targetUser.getId())
                .postCount(postCount)
                .build();
    }

    @Override
    public void softDeletePost(
            Long postId
    ) {

        logger.info(
                "Soft deleting post | postId: {}",
                postId
        );

        Post post = postAccessValidator.getPost(postId);

        User currentUser =
                authUtil.getCurrentUser();

        ownershipValidator.validate(
                post.getUser().getId(),
                currentUser,
                "You are not authorized to delete this post"
        );
        logger.debug(
                "Ownership validation passed | userId: {} | postId: {}",
                currentUser.getId(),
                postId
        );

        for (Hashtag hashtag : post.getHashtags()) {

            hashtag.setPostCount(
                    Math.max(
                            0,
                            hashtag.getPostCount() - 1
                    )
            );

            hashtagRepository.save(hashtag);
        }

        post.setDeleted(true);

        postRepository.save(post);

        logger.info(
                "Post soft deleted successfully | postId: {}",
                postId
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

        Post post = postAccessValidator.getPost(postId);

        // OWNERSHIP CHECK
        ownershipValidator.validate(
                post.getUser().getId(),
                currentUser,
                "You are not authorized"
        );
        logger.debug(
                "Ownership validation passed for restore request | userId: {} | postId: {}",
                currentUser.getId(),
                postId
        );

        if (!post.getDeleted()) {

            logger.warn(
                    "Restore request failed | postId: {} | reason: Post is not deleted",
                    postId
            );

            throw new OperationFailException(
                    "Post is not deleted."
            );
        }

        if (post.getRestoreRequested()) {

            logger.warn(
                    "Restore request already submitted"
            );

            throw new OperationFailException(
                    "Restore request already submitted"
            );

        }

        post.setRestoreRequested(true);

        postRepository.save(post);

        logger.info(
                "Restore request created successfully | postId: {}",
                postId
        );
    }

    //private method
    private PostResponse mapToResponse(
            Post post,
            Set<Long> likedPostIds,
            User currentUser
    ) {

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
                        likedPostIds.contains(post.getId())
                )

                .mine(
                        currentUser.getId()
                                .equals(post.getUser().getId())
                )

                .createdAt(post.getCreatedAt())

                .updatedAt(post.getUpdatedAt())

                .savedAt(post.getCreatedAt())

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

    private List<Hashtag> processHashtags(String caption) {

        logger.debug("Processing hashtags from caption");

        Set<String> extractedTags =
                HashtagUtil.extractHashtags(caption);

        List<Hashtag> hashtags = new ArrayList<>();

        for (String tagName : extractedTags) {

            Optional<Hashtag> existing =
                    hashtagRepository.findByName(tagName);

            if (existing.isPresent()) {

                Hashtag hashtag = existing.get();

                hashtag.setPostCount(
                        hashtag.getPostCount() + 1
                );

                hashtags.add(
                        hashtagRepository.save(hashtag)
                );

            } else {

                Hashtag hashtag = new Hashtag();

                hashtag.setName(tagName);

                hashtag.setPostCount(1L);

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
            Post post,
            List<String> uploadedFiles
    ) {

        logger.debug(
                "Uploading media files | postId: {}",
                post.getId()
        );

        List<Media> mediaList =
                new ArrayList<>();

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

            // TRACK SUCCESSFUL UPLOADS
            uploadedFiles.add(fileName);

            Media media = new Media();

            media.setUrl(fileName);

            media.setPublicId(fileName);

            media.setPost(post);

            String type =
                    file.getContentType();

            if (
                    type != null
                            &&
                            type.startsWith("video")
            ) {

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

    private Set<Long> buildSingleLikedPostSet(
            Post post,
            User currentUser
    ) {

        return likeRepository.existsByUserAndPost(
                currentUser,
                post
        )
                ? Set.of(post.getId())
                : Set.of();
    }

}

