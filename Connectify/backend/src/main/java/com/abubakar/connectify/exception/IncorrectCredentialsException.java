package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class IncorrectCredentialsException extends BaseException {

    private static final long serialVersionUID = 1L;

	public IncorrectCredentialsException(String msg) {
        super(msg, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
    
}

