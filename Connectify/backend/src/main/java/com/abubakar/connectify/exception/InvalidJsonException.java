package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class InvalidJsonException
        extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidJsonException(
            String message
    ) {
        super(
                message,
                HttpStatus.BAD_REQUEST,
                "INVALID_JSON"
        );
    }

}

