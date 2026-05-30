package com.abubakar.connectify.scheduler;

import com.abubakar.connectify.repository.RefreshTokenRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RefreshTokenCleanupScheduler {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    RefreshTokenCleanupScheduler.class
            );

    @Scheduled(
            cron = "0 0 * * * *"
    )
    public void cleanExpiredTokens() {

        long deletedCount =
                refreshTokenRepository
                        .deleteByExpiryDateBefore(
                                LocalDateTime.now()
                        );

        logger.info(
                "Expired refresh tokens removed | count: {}",
                deletedCount
        );
    }

}