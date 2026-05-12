package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class SenderNotFound extends BaseException {

    private static final long serialVersionUID = 1L;

	public SenderNotFound(String sender) {
        super("Sender not found: " + sender, HttpStatus.NOT_FOUND, "SENDER_NOT_FOUND");
    }
    
}

