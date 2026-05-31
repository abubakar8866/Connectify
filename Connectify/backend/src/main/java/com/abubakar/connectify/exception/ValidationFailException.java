package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ValidationFailException
        extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ValidationFailException(
            String message
    ) {
        super(
                message,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR"
        );
    }

}

