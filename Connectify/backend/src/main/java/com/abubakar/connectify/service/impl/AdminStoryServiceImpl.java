package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.dto.request.AdminStoryFilterRequest;
import com.abubakar.connectify.dto.response.AdminStoryResponse;
import com.abubakar.connectify.dto.response.CursorPageResponse;
import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.StoryRepository;
import com.abubakar.connectify.service.AdminStoryService;
import com.abubakar.connectify.service.FileService;
import com.abubakar.connectify.specification.StorySpecification;
import com.abubakar.connectify.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminStoryServiceImpl implements AdminStoryService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AdminStoryServiceImpl.class
            );

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private AdminValidator adminValidator;

    @Autowired
    private StoryAccessValidator storyAccessValidator;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private FileService fileService;

    // ================= GET ALL Stories =================
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<AdminStoryResponse>
    getAllStories(
            AdminStoryFilterRequest request,
            Long cursor,
            int size
    ) {

        logger.info(
                "Admin fetching all stories | cursor: {} | size: {}",
                cursor,
                size
        );

        validateAdminAccess();

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        Specification<Story> specification =
                StorySpecification.filterStories(request,cursor);

        List<Story> stories =
                storyRepository.findAll(specification, pageable)
                        .getContent();

        logger.info(
                "Stories fetched successfully | count: {}",
                stories.size()
        );

        return CursorPaginationUtil.buildResponse(
                stories,
                size,
                Story::getId,
                this::mapToResponse
        );
    }

    // ================= GET SINGLE STORY =================
    @Override
    public AdminStoryResponse getStoryById(
            Long storyId
    ) {

        logger.info(
                "Admin fetching story by id | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story = storyAccessValidator.getStory(storyId);

        logger.info(
                "Story fetched successfully | storyId: {}",
                storyId
        );

        return mapToResponse(story);
    }

    // ================= DELETE STORY =================
    @Override
    public void deleteStory(Long storyId) {

        logger.info(
                "Admin deleting story | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story = storyAccessValidator.getStory(storyId);

        // DELETE MEDIA IF EXISTS
        if (
                story.getMediaUrl() != null
                        &&
                        !story.getMediaUrl().isBlank()
        ) {

            logger.info(
                    "Deleting story media | storyId: {}",
                    storyId
            );

            fileService.deleteFile(
                    story.getMediaUrl(),
                    "stories"
            );
        }

        storyRepository.delete(story);

        logger.info(
                "Updating story status to deleted | storyId: {}",
                storyId
        );

        storyRepository.save(story);

        logger.info(
                "Admin deleted story successfully | storyId: {}",
                storyId
        );
    }

    // ================= RESTORE STORY =================
    @Override
    public void restoreStory(Long storyId) {

        logger.info(
                "Admin restoring story | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story = storyAccessValidator.getStory(storyId);

        if (!Boolean.TRUE.equals(story.getRestoreRequested())) {
            throw new OperationFailException(
                    "Restore request not found"
            );
        }

        if (!Boolean.TRUE.equals(story.getDeleted())) {

            throw new OperationFailException(
                    "Story is not deleted"
            );
        }

        // RESET STORY STATE
        story.setDeleted(false);
        story.setIsActive(true);
        story.setRestoreRequested(false);

        // GIVE FRESH 24 HOUR LIFECYCLE
        story.setExpiresAt(
                LocalDateTime.now().plusHours(24)
        );

        logger.info(
                "Restoring story with fresh lifecycle | storyId: {} | newExpiresAt: {}",
                storyId,
                story.getExpiresAt()
        );

        storyRepository.save(story);

        logger.info(
                "Admin restored story successfully | storyId: {} | expiresAt: {}",
                storyId,
                story.getExpiresAt()
        );
    }

    // ================= APPROVE RESTORE REQUEST =================
    @Override
    public void approveRestoreRequest(Long storyId) {

        logger.info(
                "Admin approving restore request | storyId: {}",
                storyId
        );

        validateAdminAccess();

        restoreStory(storyId);
        logger.info(
                "Restore request approved successfully | storyId: {}",
                storyId
        );
    }

    // ================= REJECT RESTORE REQUEST =================
    @Override
    public void rejectRestoreRequest(Long storyId) {

        logger.info(
                "Admin rejecting restore request | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story = storyAccessValidator.getStory(storyId);

        if (!Boolean.TRUE.equals(story.getRestoreRequested())) {

            throw new OperationFailException(
                    "Restore request not found"
            );
        }

        story.setRestoreRequested(false);

        logger.info(
                "Removing restore request flag | storyId: {}",
                storyId
        );

        storyRepository.save(story);

        logger.info(
                "Restore request rejected successfully | storyId: {}",
                storyId
        );
    }

    // ================= EXPIRE STORY =================
    @Override
    public void expireStory(Long storyId) {

        logger.info(
                "Admin expiring story | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story = storyAccessValidator.getStory(storyId);

        if (story.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            logger.warn("Story already expired | expired At: {}",story.getExpiresAt());

            throw new OperationFailException(
                    "Story already expired"
            );
        }

        story.setExpiresAt(LocalDateTime.now());
        story.setIsActive(false);

        logger.info(
                "Force expiring story | storyId: {}",
                storyId
        );

        storyRepository.save(story);

        logger.info(
                "Story expired successfully | storyId: {} | expiredAt: {}",
                storyId,
                story.getExpiresAt()
        );
    }

    // ================= RESTORE REQUEST =================
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<AdminStoryResponse>
    getRestoreRequests(
            Long cursor,
            int size
    ) {

        logger.info(
                "Admin fetching restore requests | cursor: {} | size: {}",
                cursor,
                size
        );

        validateAdminAccess();

        Pageable pageable =
                PaginationUtil.createCursorPageable(size);

        List<Story> stories;

        if (cursor == null) {

            stories =
                    storyRepository
                            .findByRestoreRequestedTrueOrderByIdDesc(
                                    pageable
                            );

        } else {

            stories =
                    storyRepository
                            .findByRestoreRequestedTrueAndIdLessThanOrderByIdDesc(
                                    cursor,
                                    pageable
                            );
        }

        logger.info(
                "Restore requests fetched successfully | count: {} | cursor: {}",
                stories.size(),
                cursor
        );

        return CursorPaginationUtil.buildResponse(
                stories,
                size,
                Story::getId,
                this::mapToResponse
        );
    }

    // ================= PRIVATE HELPER METHODS =================

    private AdminStoryResponse mapToResponse(
            Story story
    ) {

        return AdminStoryResponse.builder()
                .id(story.getId())
                .username(story.getUser().getUname())
                .profileImageUrl(
                        story.getUser()
                                .getProfileImageUrl()
                )
                .mediaUrl(story.getMediaUrl())
                .mediaType(story.getMediaType())
                .deleted(story.getDeleted())
                .isActive(story.getIsActive())
                .restoreRequested(
                        story.getRestoreRequested()
                )
                .viewCount(story.getViewCount())
                .reactionCount(story.getReactionCount())
                .replyCount(story.getReplyCount())
                .createdAt(story.getCreatedAt())
                .expiresAt(story.getExpiresAt())
                .build();
    }

    private void validateAdminAccess(){

        User admin = this.authUtil.getCurrentUser();
        this.adminValidator.validateAdmin(admin);
        logger.info(
                "Admin validated successfully | adminId: {}",
                admin.getId()
        );

    }

}

