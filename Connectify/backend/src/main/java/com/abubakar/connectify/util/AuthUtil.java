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

        User user =
                getAuthenticatedValidUser();

        userAccessValidator.validateActiveUser(
                user
        );

        logger.debug(
                "Authenticated active user fetched successfully | userId: {}",
                user.getId()
        );

        return user;
    }

    public User getCurrentUserAllowDeactivated() {

        User user =
                getAuthenticatedValidUser();

        logger.debug(
                "Authenticated user fetched successfully (deactivated allowed) | userId: {}",
                user.getId()
        );

        return user;
    }

    private User getAuthenticatedValidUser() {

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

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof User user)) {

            logger.warn(
                    "Invalid authentication principal"
            );

            throw new UserNotAuthenticatedException(
                    "Invalid authentication principal"
            );
        }

        return userAccessValidator.getValidUser(
                user.getId()
        );
    }

}

