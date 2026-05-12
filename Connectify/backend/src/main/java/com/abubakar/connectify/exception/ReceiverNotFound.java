package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class ReceiverNotFound extends BaseException {

    private static final long serialVersionUID = 1L;

	public ReceiverNotFound(String receiver) {
        super("Receiver not found: " + receiver, HttpStatus.NOT_FOUND, "RECEIVER_NOT_FOUND");
    }
    
}

