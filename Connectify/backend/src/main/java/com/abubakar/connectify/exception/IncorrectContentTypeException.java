package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class IncorrectContentTypeException extends BaseException {

    private static final long serialVersionUID = 1L;

	public IncorrectContentTypeException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_CONTENT_TYPE");
    }
    
}

