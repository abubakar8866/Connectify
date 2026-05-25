package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.StoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class StoryAccessValidator {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserAccessValidator userAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    StoryAccessValidator.class
            );

    // ================= USER APIs =================
    public Story getActiveStory(
            Long storyId
    ) {

        logger.debug(
                "Validating active story access | storyId: {}",
                storyId
        );

        Story story =
                getStory(storyId);

        // DELETED STORY VALIDATION
        if (Boolean.TRUE.equals(story.getDeleted())) {

            logger.warn(
                    "Story access denied | deleted story | storyId: {}",
                    storyId
            );

            throw new OperationFailException(
                    "Story is deleted"
            );
        }

        // EXPIRED STORY VALIDATION
        if (story.getExpiresAt().isBefore(LocalDateTime.now())) {

            logger.warn(
                    "Story access denied | expired story | storyId: {}",
                    storyId
            );

            throw new OperationFailException(
                    "Story has expired"
            );
        }

        // STORY OWNER VALIDATION
        userAccessValidator.validateActiveUser(story.getUser());

        logger.debug(
                "Active story validation successful | storyId: {}",
                storyId
        );

        return story;
    }

    // ================= ADMIN APIs =================
    public Story getStory(
            Long storyId
    ) {

        logger.debug(
                "Fetching story | storyId: {}",
                storyId
        );

        return storyRepository.findById(storyId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Story not found | storyId: {}",
                            storyId
                    );

                    return new ResourceNotFound(
                            "Story not found with id: " + storyId
                    );
                });
    }

}

