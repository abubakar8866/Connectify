package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class EmailNotFound extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

	public EmailNotFound(String msg) {

        super(msg, HttpStatus.NOT_FOUND, "EMAIL_NOT_FOUND");

    }
    
}

