package com.nortal.test.core.model;

import org.springframework.http.HttpStatus;

public class CommonCallResponse<S, E> {
	private final S successResponse;
	private final E errorResponse;
	private final HttpStatus httpStatus;

	public static <S, E> CommonCallResponse<S, E> onSuccess(final S successResponse, final HttpStatus httpStatus) {
		return new CommonCallResponse<>(successResponse, null, httpStatus);
	}

	public static <S, E> CommonCallResponse<S, E> onError(final E errorResponse, final HttpStatus httpStatus) {
		return new CommonCallResponse<>(null, errorResponse, httpStatus);
	}

	private CommonCallResponse(final S successResponse, final E errorResponse, final HttpStatus httpStatus) {
		this.successResponse = successResponse;
		this.errorResponse = errorResponse;
		this.httpStatus = httpStatus;
	}

	public boolean isError() {
		return errorResponse != null;
	}

	public S getSuccessResponse() {
		return successResponse;
	}

	public E getErrorResponse() {
		return errorResponse;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
