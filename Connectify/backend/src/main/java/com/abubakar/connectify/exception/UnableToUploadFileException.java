package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class UnableToUploadFileException extends BaseException {

    private static final long serialVersionUID = 1L;

	public UnableToUploadFileException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_ERROR");
    }
    
}

