package com.nortal.test.core.rest.error;

/**
 * Exception representing generic validation failure.
 */
public class ValidationException extends RuntimeException {
	public ValidationException(final String message) {
		super(message);
	}
}