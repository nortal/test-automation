package com.nortal.test.core.exceptions;

public class MissingSkuInSolrException extends TestExecutionException {
	public MissingSkuInSolrException(final String message) {
		super(message);
	}
}
