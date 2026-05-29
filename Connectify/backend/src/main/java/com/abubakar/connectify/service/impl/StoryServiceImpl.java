package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.StoryReactionRequest;
import com.abubakar.connectify.dto.request.StoryReplyRequest;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.dto.response.StoryResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.StoryReaction;
import com.abubakar.connectify.entity.StoryReply;
import com.abubakar.connectify.entity.StoryView;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.MediaType;
import com.abubakar.connectify.enums.NotificationType;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.StoryReactionRepository;
import com.abubakar.connectify.repository.StoryReplyRepository;
import com.abubakar.connectify.repository.StoryRepository;
import com.abubakar.connectify.repository.StoryViewRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.FileService;
import com.abubakar.connectify.service.NotificationService;
import com.abubakar.connectify.service.StoryService;

import com.abubakar.connectify.util.*;
import org.modelmapper.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class StoryServiceImpl implements StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryViewRepository storyViewRepository;

    @Autowired
    private StoryReactionRepository storyReactionRepository;

    @Autowired
    private StoryReplyRepository storyReplyRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OwnershipValidator ownershipValidator;

    @Autowired
    private UserAccessValidator userAccessValidator;

    @Autowired
    private StoryAccessValidator storyAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(StoryServiceImpl.class);

    // ================= CREATE STORY =================
    @Override
    public StoryResponse createStory(
            MultipartFile file
    ) {

        logger.info("Creating new story");

        User currentUser =
                this.authUtil.getCurrentUser();

        logger.info(
                "Create story process started | userId: {}",
                currentUser.getId()
        );

        // UPLOAD MAIN MEDIA
        String uploadedFile =
                fileService.uploadFile(
                        file,
                        currentUser.getId(),
                        null,
                        "stories"
                );

        logger.debug(
                "Story media uploaded successfully | userId: {} | mediaUrl: {}",
                currentUser.getId(),
                uploadedFile
        );

        String contentType =
                file.getContentType();

        MediaType mediaType;

        // VIDEO STORY
        if (contentType != null &&
                contentType.startsWith("video")) {

            mediaType = MediaType.VIDEO;
        }

        // IMAGE STORY
        else {

            mediaType = MediaType.IMAGE;
        }

        logger.debug(
                "Detected story media type | userId: {} | mediaType: {}",
                currentUser.getId(),
                mediaType
        );

        Story story = Story.builder()
                .mediaUrl(uploadedFile)
                .mediaType(mediaType)
                .expiresAt(
                        LocalDateTime.now().plusHours(24)
                )
                .isActive(true)
                .viewCount(0L)
                .reactionCount(0L)
                .replyCount(0L)
                .user(currentUser)
                .build();

        Story savedStory =
                storyRepository.save(story);

        logger.info(
                "Story created successfully | storyId: {} | userId: {}",
                savedStory.getId(),
                currentUser.getId()
        );

        return mapToResponse(
                savedStory,
                currentUser,
                Set.of(),
                Set.of()
        );
    }

    // ================= GET ACTIVE STORIES =================
    @Override
    public CursorPageResponse<StoryResponse> getActiveStories(
            Long cursor,
            int size
     ) {

        logger.info(
                "Fetching active stories | cursor: {} | size: {}",
                cursor,
                size
        );

        User currentUser = this.authUtil.getCurrentUser();

        logger.debug(
                "Validating story owner account status while getting active stories | ownerId: {}",
                currentUser.getId()
        );
        userAccessValidator.getValidUser(currentUser.getId());

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Story> stories;

        if (cursor == null) {

            stories =
                    storyRepository
                            .findActiveStories(
                                    LocalDateTime.now(),
                                    pageable
                            );

        } else {

            stories =
                    storyRepository
                            .findActiveStoriesByCursor(
                                    LocalDateTime.now(),
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Active stories fetched successfully | count: {}",
                stories.size()
        );

        return buildStoryResponse(
                stories,
                size,
                currentUser
        );

    }

    // ================= VIEW STORY =================
    @Override
    public void viewStory(Long storyId) {

        logger.info(
                "Viewing story | storyId: {}",
                storyId
        );

        User currentUser = this.authUtil.getCurrentUser();

        Story story = storyAccessValidator.getActiveStory(storyId);

        logger.debug(
                "Validating story owner account status | ownerId: {}",
                story.getUser().getId()
        );
        userAccessValidator.getValidUser(story.getUser().getId());

        if (story.getUser().getId()
                .equals(currentUser.getId())) {
            logger.debug(
                    "Skipping story view count increment for owner | storyId: {} | ownerId: {}",
                    storyId,
                    currentUser.getId()
            );
            return;
        }

        boolean alreadyViewed =
                storyViewRepository
                        .existsByStoryAndViewer(
                                story,
                                currentUser
                        );

        if (!alreadyViewed) {

            StoryView storyView =
                    StoryView.builder()
                            .story(story)
                            .viewer(currentUser)
                            .build();

            storyViewRepository.save(storyView);

            story.setViewCount(
                    story.getViewCount() + 1
            );

            storyRepository.save(story);

            logger.info(
                    "Story viewed successfully | storyId: {} | viewerId: {}",
                    storyId,
                    currentUser.getId()
            );
        }
    }

    // ================= REACT STORY =================
    @Override
    public void reactStory(
            Long storyId,
            StoryReactionRequest request
    ) {

        logger.info(
                "Reacting to story | storyId: {}",
                storyId
        );

        User currentUser = this.authUtil.getCurrentUser();

        Story story = storyAccessValidator.getActiveStory(storyId);

        logger.debug(
                "Validating story owner account before reaction | ownerId: {}",
                story.getUser().getId()
        );
        userAccessValidator.getValidUser(story.getUser().getId());

        if (story.getUser().getId()
                .equals(currentUser.getId())) {

            logger.debug(
                    "You cannot react to your own story | storyId: {} | userId: {}",
                    storyId,
                    currentUser.getId()
            );


            throw new OperationFailException(
                    "You cannot react to your own story"
            );
        }

        StoryReaction existingReaction =
                storyReactionRepository
                        .findByStoryAndUser(
                                story,
                                currentUser
                        )
                        .orElse(null);

        // TOGGLE OFF
        if (existingReaction != null) {

            storyReactionRepository.delete(
                    existingReaction
            );

            story.setReactionCount(
                    Math.max(0, story.getReactionCount() - 1)
            );

            storyRepository.save(story);

            logger.info(
                    "Story reaction removed | storyId: {} | userId: {}",
                    storyId,
                    currentUser.getId()
            );

            return;
        }

        StoryReaction reaction =
                StoryReaction.builder()
                        .story(story)
                        .user(currentUser)
                        .reactionType(
                                request.getReactionType()
                        )
                        .build();

        storyReactionRepository.save(reaction);

        story.setReactionCount(
                story.getReactionCount() + 1
        );

        storyRepository.save(story);

        // NOTIFICATION
       notificationService.createNotification(
            story.getUser().getId(),
            currentUser.getId(),
            currentUser.getUname() + " reacted to your story",
            NotificationType.STORY_REACTION,
            null,
            null
       );

        logger.info(
                "Story reaction added successfully | storyId: {} | userId: {}",
                storyId,
                currentUser.getId()
        );
    }

    // ================= REPLY STORY =================
    @Override
    public void replyStory(
            Long storyId,
            StoryReplyRequest request
    ) {

        logger.info(
                "Replying to story | storyId: {}",
                storyId
        );

        User currentUser = this.authUtil.getCurrentUser();

        Story story = storyAccessValidator.getActiveStory(storyId);

        logger.debug(
                "Validating story owner account before reply | ownerId: {}",
                story.getUser().getId()
        );
        userAccessValidator.getValidUser(story.getUser().getId());

        if (story.getUser().getId()
                .equals(currentUser.getId())) {

            logger.debug(
                    "You cannot reply to your own story | storyId: {} | senderId: {}",
                    storyId,
                    currentUser.getId()
            );

            throw new OperationFailException(
                    "You cannot reply to your own story"
            );
        }

        StoryReply reply =
                StoryReply.builder()
                        .story(story)
                        .sender(currentUser)
                        .message(request.getMessage())
                        .build();

        storyReplyRepository.save(reply);

        story.setReplyCount(
                story.getReplyCount() + 1
        );

        storyRepository.save(story);

        // NOTIFICATION
        notificationService.createNotification(
                story.getUser().getId(),
                currentUser.getId(),
                currentUser.getUname() + " replied to your story",
                NotificationType.STORY_REPLY,
                null,
                null
        );

        logger.info(
                "Story reply added successfully | storyId: {} | senderId: {}",
                storyId,
                currentUser.getId()
        );
    }

    // ================= RESTORE STORY REQUEST =================
    @Override
    public void requestRestoreStory(
            Long storyId
    ) {

        logger.info(
                "Story restore request | storyId: {}",
                storyId
        );

        User currentUser =
                authUtil.getCurrentUser();

        Story story = storyAccessValidator.getStory(storyId);

        logger.debug(
                "Validating story ownership for restore request | ownerId: {} | requesterId: {}",
                story.getUser().getId(),
                currentUser.getId()
        );

        ownershipValidator.validate(
                story.getUser().getId(),
                currentUser,
                "You are not authorized"
        );

        if (!story.getDeleted()) {

            logger.warn("Story is not deleted");

            throw new OperationFailException(
                    "Story is not deleted"
            );
        }

        if (story.getRestoreRequested()) {

            logger.warn("Restore request already submitted");

            throw new OperationFailException(
                    "Restore request already submitted"
            );
        }

        story.setRestoreRequested(true);

        story.setRestoreRequestedAt(
                LocalDateTime.now()
        );

        storyRepository.save(story);

        logger.info(
                "Story restore request submitted successfully | storyId: {} | requesterId: {}",
                storyId,
                currentUser.getId()
        );
    }

    // ================= DELETE STORY =================
    @Override
    public void deleteStory(Long storyId) {

        logger.info(
                "Deleting story | storyId: {}",
                storyId
        );

        User currentUser =
                authUtil.getCurrentUser();

        Story story =
                storyAccessValidator.getStory(
                        storyId
                );

        logger.debug(
                "Validating story ownership for deletion | ownerId: {} | requesterId: {}",
                story.getUser().getId(),
                currentUser.getId()
        );

        ownershipValidator.validate(
                story.getUser().getId(),
                currentUser,
                "You are not authorized to access this story"
        );

        // ALREADY DELETED VALIDATION
        if (Boolean.TRUE.equals(story.getDeleted())) {

            logger.warn(
                    "Story already deleted | storyId: {}",
                    storyId
            );

            throw new OperationFailException(
                    "Story already deleted"
            );
        }

        story.setDeleted(true);
        story.setIsActive(false);

        storyRepository.save(story);

        logger.info(
                "Story deleted successfully | storyId: {}",
                storyId
        );
    }

    // ================= GET STORY VIEWERS =================
    @Override
    public CursorPageResponse<UserResponse> getStoryViewers(
            Long storyId,
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching story viewers | storyId: {} | cursor: {} | size: {}",
                storyId,
                cursor,
                size
        );

        Story story = storyAccessValidator.getActiveStory(storyId);

        logger.debug(
                "Validating story ownership for viewers access | ownerId: {}",
                story.getUser().getId()
        );
        this.ownershipValidator.validate(
                story.getUser().getId(),
                this.authUtil.getCurrentUser(),
                "You are not authorized to access this story"
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<StoryView> viewers;

        if (cursor == null) {

            viewers =
                    storyViewRepository
                            .findByStoryOrderByIdDesc(
                                    story,
                                    pageable
                            );

        } else {

            viewers =
                    storyViewRepository
                            .findByStoryAndIdLessThanOrderByIdDesc(
                                    story,
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Story viewers fetched successfully | storyId: {} | viewerCount: {}",
                storyId,
                viewers.size()
        );

        return CursorPaginationUtil.buildResponse(
                viewers,
                size,
                StoryView::getId,
                viewer ->
                        modelMapper.map(
                                viewer.getViewer(),
                                UserResponse.class
                        )
        );
    }

    // ================= GET OWN STORY =================
    @Override
    public CursorPageResponse<StoryResponse> getMyStories(
            Long cursor,
            int size
    ) {

        User currentUser =
                authUtil.getCurrentUser();

        logger.info(
                "Fetching current user stories | userId: {} | cursor: {} | size: {}",
                currentUser.getId(),
                cursor,
                size
        );

        Pageable pageable =
                PaginationUtil.createCursorPageable(
                        size
                );

        List<Story> stories;

        if (cursor == null) {

            stories =
                    storyRepository
                            .findMyActiveStories(
                                    currentUser,
                                    LocalDateTime.now(),
                                    pageable
                            );

        } else {

            stories =
                    storyRepository
                            .findMyActiveStoriesByCursor(
                                    currentUser,
                                    LocalDateTime.now(),
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Current user stories fetched successfully | userId: {} | storyCount: {}",
                currentUser.getId(),
                stories.size()
        );

        return buildStoryResponse(
                stories,
                size,
                currentUser
        );

    }

    // ================= GET Active User STORY =================
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<StoryResponse>
    getUserActiveStories(
            Long userId,
            Long cursor,
            int size
    ) {

        logger.info(
                "Fetching user active stories | userId: {} | cursor: {} | size: {}",
                userId,
                cursor,
                size
        );

        User user = this.userAccessValidator.getValidUser(userId);

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        List<Story> stories;

        if (cursor == null) {

            stories =
                    storyRepository
                            .findUserActiveStories(
                                    user,
                                    LocalDateTime.now(),
                                    pageable
                            );

        } else {

            stories =
                    storyRepository
                            .findUserActiveStoriesByCursor(
                                    user,
                                    LocalDateTime.now(),
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "User active stories fetched successfully | userId: {} | storyCount: {}",
                userId,
                stories.size()
        );

        User currentUser =
                authUtil.getCurrentUser();

        return buildStoryResponse(
                stories,
                size,
                currentUser
        );

    }

    // ================= PRIVATE METHODS =================
    private StoryResponse mapToResponse(
            Story story,
            User currentUser,
            Set<Long> viewedStoryIds,
            Set<Long> reactedStoryIds
    ) {

        return StoryResponse.builder()
                .id(story.getId())

                .mediaUrl(
                        story.getMediaUrl()
                )

                .mediaType(
                        story.getMediaType()
                )

                .username(
                        story.getUser().getUname()
                )

                .profileImageUrl(
                        story.getUser().getProfileImageUrl()
                )

                .isVerified(
                        story.getUser().getIsVerified()
                )

                .viewCount(
                        story.getViewCount()
                )

                .reactionCount(
                        story.getReactionCount()
                )

                .replyCount(
                        story.getReplyCount()
                )

                .viewed(
                        viewedStoryIds.contains(
                                story.getId()
                        )
                )

                .reacted(
                        reactedStoryIds.contains(
                                story.getId()
                        )
                )

                .createdAt(
                        story.getCreatedAt()
                )

                .expiresAt(
                        story.getExpiresAt()
                )

                .userId(
                        story.getUser().getId()
                )

                .isMine(
                        story.getUser()
                                .getId()
                                .equals(currentUser.getId())
                )

                .build();
    }

    private CursorPageResponse<StoryResponse>
    buildStoryResponse(
            List<Story> stories,
            int size,
            User currentUser
    ) {

        List<Long> storyIds =
                stories.stream()
                        .map(Story::getId)
                        .toList();

        Set<Long> viewedStoryIds =
                new HashSet<>(
                        storyViewRepository.findViewedStoryIds(
                                currentUser.getId(),
                                storyIds
                        )
                );

        Set<Long> reactedStoryIds =
                new HashSet<>(
                        storyReactionRepository.findReactedStoryIds(
                                currentUser.getId(),
                                storyIds
                        )
                );

        return CursorPaginationUtil.buildResponse(
                stories,
                size,
                Story::getId,
                story -> mapToResponse(
                        story,
                        currentUser,
                        viewedStoryIds,
                        reactedStoryIds
                )
        );

    }

}

