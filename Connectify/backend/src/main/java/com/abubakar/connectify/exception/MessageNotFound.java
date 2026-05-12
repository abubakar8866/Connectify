package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class MessageNotFound extends BaseException {

    private static final long serialVersionUID = 1L;

	public MessageNotFound(String id) {
        super("Message not found with id: " + id, HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND");
    }
    
}

