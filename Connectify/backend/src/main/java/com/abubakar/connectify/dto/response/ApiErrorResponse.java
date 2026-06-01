package com.abubakar.connectify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	private String message;
    private boolean success;
    private int status;
    private String errorCode;
    private long timestamp;

}

