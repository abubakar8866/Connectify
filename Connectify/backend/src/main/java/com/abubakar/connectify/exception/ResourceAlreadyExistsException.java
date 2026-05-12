package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends BaseException {

    private static final long serialVersionUID = 1L;

	public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS");
    }
    
}

