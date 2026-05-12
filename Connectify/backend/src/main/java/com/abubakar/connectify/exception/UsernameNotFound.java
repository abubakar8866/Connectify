package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class UsernameNotFound extends BaseException {

    private static final long serialVersionUID = 1L;

	public UsernameNotFound(String username) {
        super("Username not found: " + username, HttpStatus.NOT_FOUND, "USERNAME_NOT_FOUND");
    }
    
}

