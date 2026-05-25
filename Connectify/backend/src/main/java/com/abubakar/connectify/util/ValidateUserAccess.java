package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateUserAccess {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ValidateUserAccess.class
            );

    public User getValidUser(
            Long userId
    ) {

        logger.debug(
                "Validating user access | userId: {}",
                userId
        );

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "User validation failed | user not found | userId: {}",
                                    userId
                            );

                            return new ResourceNotFound(
                                    "User is not found with ID:" + userId
                            );

                        });

        if (Boolean.TRUE.equals(user.getDeleted())) {

            logger.warn(
                    "User validation failed | deleted account | userId: {}",
                    userId
            );

            throw new ResourceNotFound(
                    "User account no longer exists"
            );
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {

            logger.warn(
                    "User validation failed | inactive account | userId: {}",
                    userId
            );

            throw new OperationFailException(
                    "User account is inactive"
            );
        }

        if (user.getAccountStatus() == AccountStatus.BANNED) {

            logger.warn(
                    "User validation failed | banned account | userId: {}",
                    userId
            );

            throw new OperationFailException(
                    "User account is banned"
            );
        }

        logger.debug(
                "User validation successful | userId: {}",
                userId
        );

        return user;
    }

}

