package com.nortal.test.core.exceptions;

public class MissingGoldenDataException extends TestExecutionException{
	public MissingGoldenDataException(final String message) {
		super(message);
	}
}
