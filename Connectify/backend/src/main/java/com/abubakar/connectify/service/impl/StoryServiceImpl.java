package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.StoryReactionRequest;
import com.abubakar.connectify.dto.request.StoryReplyRequest;
import com.abubakar.connectify.dto.response.StoryResponse;
import com.abubakar.connectify.dto.response.UserResponse;
import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.StoryReaction;
import com.abubakar.connectify.entity.StoryReply;
import com.abubakar.connectify.entity.StoryView;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.MediaType;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.StoryReactionRepository;
import com.abubakar.connectify.repository.StoryReplyRepository;
import com.abubakar.connectify.repository.StoryRepository;
import com.abubakar.connectify.repository.StoryViewRepository;
import com.abubakar.connectify.repository.UserRepository;
import com.abubakar.connectify.service.FileService;
import com.abubakar.connectify.service.StoryService;

import com.abubakar.connectify.util.AuthUtil;
import com.abubakar.connectify.util.OwnershipValidator;
import org.modelmapper.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class StoryServiceImpl implements StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private StoryViewRepository storyViewRepository;

    @Autowired
    private StoryReactionRepository storyReactionRepository;

    @Autowired
    private StoryReplyRepository storyReplyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OwnershipValidator ownershipValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(StoryServiceImpl.class);

    // ================= CREATE STORY =================
    @Override
    public StoryResponse createStory(MultipartFile file) {

        logger.info("Creating new story");

        User currentUser = this.authUtil.getCurrentUser();

        String uploadedFile =
                fileService.uploadFile(
                        file,
                        currentUser.getId(),
                        null,
                        "stories"
                );

        String contentType = file.getContentType();

        MediaType mediaType;

        if (contentType != null &&
                contentType.startsWith("video")) {

            mediaType = MediaType.VIDEO;

        } else {

            mediaType = MediaType.IMAGE;
        }

        Story story = Story.builder()
                .mediaUrl(uploadedFile)
                .publicId(uploadedFile)
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
                "Story created successfully | storyId: {}",
                savedStory.getId()
        );

        return mapToResponse(savedStory);
    }

    // ================= GET ACTIVE STORIES =================
    @Override
    public List<StoryResponse> getActiveStories() {

        logger.info("Fetching active stories");

        List<Story> stories =
                storyRepository
                        .findByExpiresAtAfterOrderByCreatedAtDesc(
                                LocalDateTime.now()
                        );

        logger.info(
                "Total active stories fetched: {}",
                stories.size()
        );

        return stories.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= VIEW STORY =================
    @Override
    public void viewStory(Long storyId) {

        logger.info(
                "Viewing story | storyId: {}",
                storyId
        );

        User currentUser = this.authUtil.getCurrentUser();

        Story story = getStoryById(storyId);

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

        Story story = getStoryById(storyId);

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
                    story.getReactionCount() - 1
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

        logger.info(
                "Story reaction added | storyId: {} | userId: {}",
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

        Story story = getStoryById(storyId);

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

        logger.info(
                "Story reply added successfully | storyId: {}",
                storyId
        );
    }

    // ================= DELETE STORY =================
    @Override
    public void deleteStory(Long storyId) {

        logger.info(
                "Deleting story | storyId: {}",
                storyId
        );

        Story story = getStoryById(storyId);

        this.ownershipValidator.validate(
                story.getUser().getId(),
                this.authUtil.getCurrentUser(),
                "You are not authorized to access this story"
        );

        fileService.deleteFile(
                story.getPublicId(),
                "stories"
        );

        storyRepository.delete(story);

        logger.info(
                "Story deleted successfully | storyId: {}",
                storyId
        );
    }

    // ================= GET STORY VIEWERS =================
    @Override
    public List<UserResponse> getStoryViewers(Long storyId) {

        logger.info(
                "Fetching story viewers | storyId: {}",
                storyId
        );

        Story story = getStoryById(storyId);

        this.ownershipValidator.validate(
                story.getUser().getId(),
                this.authUtil.getCurrentUser(),
                "You are not authorized to access this story"
        );

        List<StoryView> viewers =
                storyViewRepository.findByStory(story);

        return viewers.stream()
                .map(StoryView::getViewer)
                .map(user ->
                        modelMapper.map(
                                user,
                                UserResponse.class
                        )
                )
                .toList();
    }

    // ================= MAP RESPONSE =================
    private StoryResponse mapToResponse(Story story) {

        User currentUser = this.authUtil.getCurrentUser();

        boolean viewed =
                storyViewRepository
                        .existsByStoryAndViewer(
                                story,
                                currentUser
                        );

        boolean reacted =
                storyReactionRepository
                        .findByStoryAndUser(
                                story,
                                currentUser
                        )
                        .isPresent();

        return StoryResponse.builder()
                .id(story.getId())
                .mediaUrl(story.getMediaUrl())
                .mediaType(story.getMediaType())

                .username(
                        story.getUser().getUname()
                )

                .profileImageUrl(
                        story.getUser().getProfileImageUrl()
                )

                .viewCount(story.getViewCount())
                .reactionCount(story.getReactionCount())
                .replyCount(story.getReplyCount())

                .viewed(viewed)
                .reacted(reacted)

                .createdAt(story.getCreatedAt())
                .expiresAt(story.getExpiresAt())

                .build();
    }

    // ================= GET STORY BY ID =================
    private Story getStoryById(Long storyId) {

        return storyRepository.findById(storyId)
                .orElseThrow(() -> {

                    logger.error(
                            "Story not found | storyId: {}",
                            storyId
                    );

                    return new ResourceNotFound(
                            "Story not found with id: "
                                    + storyId
                    );
                });
    }

}

