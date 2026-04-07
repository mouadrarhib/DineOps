package com.mouad.dineops.dineOps.common.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mouad.dineops.dineOps.common.response.ApiResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException exception) {
		return ResponseEntity
				.status(exception.getStatus())
				.body(ApiResponse.error(exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<ValidationErrorResponse>> handleValidationException(
			MethodArgumentNotValidException exception) {
		List<ValidationErrorResponse.FieldValidationError> errors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new ValidationErrorResponse.FieldValidationError(
						error.getField(),
						error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()))
				.toList();

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error("Validation failed", new ValidationErrorResponse(errors)));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
			ConstraintViolationException exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error(exception.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error(exception.getMessage()));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException exception) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.error("Invalid credentials"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body(ApiResponse.error("Forbidden"));
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthorizationDeniedException(AuthorizationDeniedException exception) {
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body(ApiResponse.error("Forbidden"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception exception) {
		log.error("Unhandled exception", exception);
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("Unexpected error occurred"));
	}
}
