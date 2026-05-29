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
    getStories(
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

    @Override
    public void moderateStory(
            Long storyId
    ) {

        logger.info(
                "Admin moderating story | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story =
                storyAccessValidator.getStory(
                        storyId
                );

        if (story.getDeleted()) {

            logger.warn("Story already moderated");

            throw new OperationFailException(
                    "Story already moderated"
            );
        }

        if (Boolean.TRUE.equals(story.getDeletedByAdmin())) {

            logger.warn("Story already moderated by admin");

            throw new OperationFailException(
                    "Story already moderated by admin"
            );
        }

        story.setDeleted(true);

        story.setIsActive(false);

        story.setRestoreRequested(false);

        story.setRestoreRequestedAt(null);

        story.setDeletedByAdmin(true);

        story.setDeletedByAdminAt(
                LocalDateTime.now()
        );

        storyRepository.save(story);

        logger.info(
                "Story moderated successfully | storyId: {}",
                storyId
        );
    }

    @Override
    public void approveStoryRestore(
            Long storyId
    ) {

        logger.info(
                "Admin approving story restore | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story =
                storyAccessValidator.getStory(
                        storyId
                );

        if (!story.getDeleted()) {

            throw new OperationFailException(
                    "Story is not deleted"
            );
        }

        if (!story.getRestoreRequested()) {

            throw new OperationFailException(
                    "Restore request not found"
            );
        }

        story.setDeleted(false);

        story.setIsActive(true);

        story.setRestoreRequested(false);

        story.setRestoreRequestedAt(null);

        story.setDeletedByAdmin(false);

        story.setDeletedByAdminAt(null);

        story.setExpiresAt(
                LocalDateTime.now().plusHours(24)
        );

        storyRepository.save(story);

        logger.info(
                "Story restore approved successfully | storyId: {}",
                storyId
        );
    }

    @Override
    public void rejectStoryRestore(
            Long storyId
    ) {

        logger.info(
                "Admin rejecting story restore | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story =
                storyAccessValidator.getStory(
                        storyId
                );

        if (!story.getRestoreRequested()) {

            throw new OperationFailException(
                    "Restore request not found"
            );
        }

        story.setRestoreRequested(false);
        story.setRestoreRequestedAt(null);

        storyRepository.save(story);

        logger.info(
                "Story restore rejected successfully | storyId: {}",
                storyId
        );
    }

    @Override
    public void permanentlyDeleteStory(
            Long storyId
    ) {

        logger.info(
                "Admin permanently deleting story | storyId: {}",
                storyId
        );

        validateAdminAccess();

        Story story =
                storyAccessValidator.getStory(
                        storyId
                );

        if (
                story.getMediaUrl() != null
                        &&
                        !story.getMediaUrl().isBlank()
        ) {

            fileService.deleteFile(
                    story.getMediaUrl(),
                    "stories"
            );
        }

        storyRepository.delete(story);

        logger.info(
                "Story permanently deleted successfully | storyId: {}",
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
        story.setRestoreRequested(false);
        story.setRestoreRequestedAt(null);

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
                .deletedByAdmin(story.getDeletedByAdmin())
                .deletedByAdminAt(story.getDeletedByAdminAt())
                .isActive(story.getIsActive())
                .restoreRequested(story.getRestoreRequested())
                .reportCount((long) story.getReports().size())
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

