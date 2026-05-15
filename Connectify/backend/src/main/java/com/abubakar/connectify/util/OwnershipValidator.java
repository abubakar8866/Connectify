package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.exception.UnauthorizedException;

import org.springframework.stereotype.Component;

@Component
public class OwnershipValidator {

    public void validate(
            Long ownerId,
            User currentUser,
            String message
    ) {

        if (!ownerId.equals(currentUser.getId())) {

            throw new UnauthorizedException(message);
        }
    }

}

