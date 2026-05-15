package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.UserNotAuthenticatedException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AuthUtil {

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

            throw new UserNotAuthenticatedException(
                    "User not authenticated"
            );
        }

        return (User) authentication.getPrincipal();
    }
}