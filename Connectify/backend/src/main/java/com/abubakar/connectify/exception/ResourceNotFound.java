package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFound extends BaseException {

    private static final long serialVersionUID = 1L;

	public ResourceNotFound(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}

