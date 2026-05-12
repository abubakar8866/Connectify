package com.abubakar.connectify.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedProviderException extends BaseException {

    private static final long serialVersionUID = 1L;

	public UnsupportedProviderException(String provider) {
        super("Unsupported provider: " + provider, HttpStatus.BAD_REQUEST, "UNSUPPORTED_PROVIDER");
    }
    
}

