package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class EmptyException extends BaseException {

    private static final long serialVersionUID = 1L;

	public EmptyException(String message) {
        super(message, HttpStatus.NO_CONTENT, "EMPTY_DATA");
    }
    
}

