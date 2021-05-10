package com.nortal.test.core.rest;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Getter
@Value
@Builder
public class HttpResponse<T> {
	boolean isSuccessful;
	int statusCode;
	T body;
}
