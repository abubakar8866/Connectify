package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class GithubIdNotFound extends BaseException {

    private static final long serialVersionUID = 1L;

	public GithubIdNotFound(String id) {
        super("Github ID not found: " + id, HttpStatus.NOT_FOUND, "GITHUB_ID_NOT_FOUND");
    }
    
}

