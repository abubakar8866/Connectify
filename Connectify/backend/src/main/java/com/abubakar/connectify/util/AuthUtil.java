package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.UserNotAuthenticatedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AuthUtil {

    @Autowired
    private UserAccessValidator userAccessValidator;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    AuthUtil.class
            );

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
                        ||
                        Objects.equals(
                                authentication.getPrincipal(),
                                "anonymousUser"
                        )
        ) {

            logger.warn(
                    "Authentication failed | anonymous or invalid session"
            );

            throw new UserNotAuthenticatedException(
                    "User not authenticated"
            );
        }

        User user =
                (User) authentication.getPrincipal();

        assert user != null;
        logger.debug(
                "Authenticated user fetched successfully | userId: {}",
                user.getId()
        );

        return this.userAccessValidator.getValidUser(user.getId());
    }

}

