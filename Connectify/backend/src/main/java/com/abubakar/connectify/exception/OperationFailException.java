package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class OperationFailException extends BaseException {

    private static final long serialVersionUID = 1L;

	public OperationFailException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "OPERATION_FAILED");
    }
    
}

