package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class UnauthorizedException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

	public UnauthorizedException(String msg) {
        super(msg, HttpStatus.NOT_FOUND, "EMAIL_NOT_FOUND");
    }
    
}

