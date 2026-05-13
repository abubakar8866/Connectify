package com.abubakar.connectify.configuration;

import com.abubakar.connectify.entity.Story;
import com.abubakar.connectify.repository.StoryRepository;
import com.abubakar.connectify.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StoryCleanupScheduler {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private FileService fileService;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    StoryCleanupScheduler.class
            );

    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void deleteExpiredStories() {

        logger.info("Running story cleanup job");

        List<Story> expiredStories =
                storyRepository.findByExpiresAtBefore(
                        LocalDateTime.now()
                );

        logger.info(
                "Expired stories found: {}",
                expiredStories.size()
        );

        for (Story story : expiredStories) {

            // DELETE FILE
            fileService.deleteFile(
                    story.getPublicId(),
                    "stories"
            );

            // DELETE DB RECORD
            storyRepository.delete(story);

            logger.info(
                    "Deleted expired story | storyId: {}",
                    story.getId()
            );
        }

        logger.info("Story cleanup completed");
    }

}

