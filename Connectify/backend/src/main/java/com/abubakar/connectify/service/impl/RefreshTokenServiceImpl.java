package com.abubakar.connectify.service.impl;

import com.abubakar.connectify.entity.RefreshToken;
import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.repository.RefreshTokenRepository;
import com.abubakar.connectify.service.RefreshTokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepo;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    RefreshTokenServiceImpl.class
            );

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    @Value("${jwt_refresh_expiration}")
    private Long refreshTokenExpiration;

    @Override
    public RefreshToken createRefreshToken(
            User user
    ) {

        refreshTokenRepo.deleteByUser(user);

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setUser(user);

        refreshToken.setToken(
                generateRefreshToken()
        );

        refreshToken.setExpiryDate(
                LocalDateTime.now()
                        .plusSeconds(
                                refreshTokenExpiration
                        )
        );

        refreshToken = refreshTokenRepo.save(
                refreshToken
        );

        logger.info(
                "Refresh token created | userId: {}",
                user.getId()
        );

        return refreshToken;
    }

    @Override
    public RefreshToken verifyRefreshToken(
            String token
    ) {

        RefreshToken refreshToken =
                refreshTokenRepo.findByTokenWithUser(token)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Refresh token not found"
                            );

                            return new OperationFailException(
                                    "Invalid refresh token"
                            );

                        });

        if (
                refreshToken.getExpiryDate()
                        .isBefore(LocalDateTime.now())
        ) {

            refreshTokenRepo.delete(refreshToken);

            logger.warn(
                    "Refresh token expired"
            );

            throw new OperationFailException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void deleteByUser(
            User user
    ) {

        refreshTokenRepo.deleteByUser(user);

        logger.info(
                "Refresh token deleted | userId: {}",
                user.getId()
        );
    }

    private String generateRefreshToken() {

        byte[] randomBytes = new byte[64];

        SECURE_RANDOM.nextBytes(
                randomBytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

}

