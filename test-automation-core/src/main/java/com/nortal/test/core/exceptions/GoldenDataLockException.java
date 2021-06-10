package com.nortal.test.core.exceptions;

public class GoldenDataLockException extends TestExecutionException {

	public GoldenDataLockException(final String message) {
		super(message);
	}

	public GoldenDataLockException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
