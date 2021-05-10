package com.nortal.test.core.exceptions;

/**
 * A generic exception to be thrown when continuing scenario execution is no longer possible
 */
public class TestExecutionException extends RuntimeException {

	public TestExecutionException(final String message) {
		super(message);
	}

	public TestExecutionException(final Throwable cause) {
		super(cause);
	}

	public TestExecutionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
