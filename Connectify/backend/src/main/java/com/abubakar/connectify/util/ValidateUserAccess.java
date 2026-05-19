package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.AccountStatus;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.exception.ResourceNotFound;
import com.abubakar.connectify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateUserAccess {

    @Autowired
    private UserRepository userRepository;

    public User getValidUser(
            Long userId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFound(
                                        "User is not found with ID:" + userId
                                )
                        );

        if (Boolean.FALSE.equals(user.getIsActive())) {

            throw new OperationFailException(
                    "User account is inactive"
            );
        }

        if (Boolean.TRUE.equals(user.getAccountStatus().equals(AccountStatus.BANNED))) {

            throw new OperationFailException(
                    "User account is banned"
            );
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {

            throw new ResourceNotFound(
                    "User account no longer exists"
            );
        }

        return user;
    }

}

