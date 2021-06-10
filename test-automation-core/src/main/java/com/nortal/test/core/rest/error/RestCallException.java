package com.nortal.test.core.rest.error;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.nortal.test.core.exceptions.TestExecutionException;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;

public class RestCallException extends TestExecutionException {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private int code;

	private RestCallException(final String message) {
		super(message);
	}

	private RestCallException(final Exception exception) {
		super(exception);
	}

	public int getCode() {
		return code;
	}

	public static RestCallException newException(final Exception exception) {
		return new RestCallException(exception);
	}

	public static RestCallException newException(final Response<?> response) {
		String errorResponse = getRawErrorResponse(response);
		errorResponse = prettifyErrorResponse(errorResponse);

		final Request request = response.raw().request();
		final String url = request.url().toString();
		final int code = response.code();
		final String message = "Rest call " + request.method() + " " + url +
				"\nhas failed with status: " + code +
				"\nBody: " + errorResponse;

		final RestCallException restCallException = new RestCallException(message);
		restCallException.code = code;
		return restCallException;
	}

	private static String prettifyErrorResponse(String errorResponse) {
		try {
			return "\n" + GSON.toJson(GSON.fromJson(errorResponse, Object.class));
		} catch (JsonSyntaxException e) {
			return errorResponse;
		}
	}

	private static String getRawErrorResponse(final Response<?> response) {
		final ResponseBody errorBody = response.errorBody();
		if (errorBody == null) {
			return "";
		}

		try {
			return errorBody.string();
		} catch (IOException e) {
			return "";
		}
	}
}
