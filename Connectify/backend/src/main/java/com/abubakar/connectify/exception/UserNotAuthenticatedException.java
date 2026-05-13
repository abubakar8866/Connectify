package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class UserNotAuthenticatedException extends BaseException {

    public UserNotAuthenticatedException(String message) {
        super(
                message,
                HttpStatus.UNAUTHORIZED,
                "USER_NOT_AUTHENTICATED"
        );
    }
}


