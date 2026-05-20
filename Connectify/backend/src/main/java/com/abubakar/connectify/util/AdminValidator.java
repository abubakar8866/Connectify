package com.abubakar.connectify.util;

import com.abubakar.connectify.entity.User;
import com.abubakar.connectify.enums.Role;
import com.abubakar.connectify.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
public class AdminValidator {

    public void validateAdmin(
            User user
    ) {

        if (user.getRole() != Role.ADMIN) {

            throw new UnauthorizedException(
                    "Admin access required"
            );
        }
    }

}

