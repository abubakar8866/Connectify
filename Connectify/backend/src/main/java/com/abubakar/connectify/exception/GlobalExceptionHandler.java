package com.abubakar.connectify.exception;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.abubakar.connectify.dto.response.ApiErrorResponse;

import org.springframework.validation.FieldError;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// Handle all custom exceptions
	@ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseException(
			BaseException ex) {

		logger.error(
				"Business exception occurred | errorCode: {} | status: {} | message: {}",
				ex.getErrorCode(),
				ex.getStatus(),
				ex.getMessage(),
				ex
		);

        ApiErrorResponse response = new ApiErrorResponse(
                ex.getMessage(),
                false,
                ex.getStatus().value(),
                ex.getErrorCode(),
				System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, ex.getStatus());
    }
	
	// Validation errors -- occur when @Valid annotation failed
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> exceptionHandler(
			MethodArgumentNotValidException ex){

		logger.warn(
				"Validation failed | errorCount: {}",
				ex.getBindingResult().getErrorCount()
		);

		Map<String, String> map = new HashMap<String, String>();

		ex.getBindingResult()
				.getAllErrors()
				.forEach(error -> {

					String field =
							((FieldError) error)
									.getField();

					String message =
							error.getDefaultMessage();

					logger.warn(
							"Validation error | field: {} | message: {}",
							field,
							message
					);

					map.put(
							field,
							message
					);

				});

		return ResponseEntity.badRequest().body(map);
	}
	
	// Enum / parameter errors
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<String> handleEnumError(
			MethodArgumentTypeMismatchException ex) {

		logger.warn(
				"Parameter type mismatch | parameter: {} | value: {}",
				ex.getName(),
				ex.getValue()
		);

	    if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {

	        Object[] enumConstants = ex.getRequiredType().getEnumConstants();

	        String allowedValues = Arrays.stream(enumConstants)
	                .map(Object::toString)
	                .collect(Collectors.joining(", "));

	        String message = "Invalid value '" + ex.getValue() + 
	                "' for field '" + ex.getName() + 
	                "'. Allowed values: " + allowedValues;

			logger.warn(
					"Invalid enum value received | parameter: {} | value: {} | allowedValues: {}",
					ex.getName(),
					ex.getValue(),
					allowedValues
			);

	        return ResponseEntity.badRequest().body(message);
	    }

	    return ResponseEntity.badRequest().body("Invalid request parameter");
	}

	// Fallback
	@ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {

		logger.error(
				"Unexpected exception occurred | type: {} | message: {}",
				ex.getClass().getSimpleName(),
				ex.getMessage(),
				ex
		);

		ApiErrorResponse response = new ApiErrorResponse(
                 ex.getMessage(),
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
				System.currentTimeMillis()
        );

        return ResponseEntity.internalServerError().body(response);
    }
	
}

