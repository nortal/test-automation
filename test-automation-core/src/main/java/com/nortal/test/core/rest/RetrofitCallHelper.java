package com.nortal.test.core.rest;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nortal.test.core.rest.error.RestCallException;
import com.nortal.test.core.rest.interceptors.PostmanRetrofitInterceptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Retrofit helper utilities.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RetrofitCallHelper {
	private static final Gson GSON = new GsonBuilder().create();

	public static <T> Response<T> rawExecute(final Call<T> apiCall) {
		try {
			//TODO add to collection true
			return executeRequest(apiCall, true);
		} catch (Exception e) {
			throw RestCallException.newException(e);
		}
	}

	public static <T> T safelyExecute(final Call<T> apiCall) {
		//TODO disable in future by default
		return request(apiCall, RetrofitCallHelper::getResponseBody, true);
	}

	public static <T> HttpResponse<T> safelyExecuteHttpResponse(final Call<T> apiCall) {
		//TODO disable in future by default
		return request(apiCall, RetrofitCallHelper::getHttpResponse, true);
	}

	public static <T> HttpResponse<T> safelyExecuteBadHttpResponse(final Call<T> apiCall, Class<T> expectedResponseType) {
		return requestHttpResponse(apiCall, expectedResponseType, RetrofitCallHelper::getBadHttpResponse);
	}

	public static <T> T safelyExecuteBadRequest(final Call<?> apiCall, Class<T> expectedResponseType) {
		return request(apiCall, expectedResponseType, RetrofitCallHelper::getErrorBody);
	}

	public static <T> Response<T> unsafeExecute(final Call<T> apiCall) {
		try {
			return apiCall.execute();
		} catch (IOException e) {
			throw RestCallException.newException(e);
		}
	}

	public static String convertRequestBodyToString(final Request request) {
		if (request.body() == null) {
			return null;
		}

		try (final Buffer buffer = new Buffer()) {
			final Request copy = request.newBuilder().build();

			if (copy.body() != null) {
				copy.body().writeTo(buffer);
			}

			return buffer.readUtf8();
		} catch (final IOException e) {
			log.error("Failed to read body", e);
			return null;
		}
	}

	private static <T> HttpResponse<T> getHttpResponse(final Response<T> response) {
		if (response.isSuccessful()) {
			return HttpResponse.<T>builder()
					.isSuccessful(response.isSuccessful())
					.statusCode(response.code())
					.body(response.body())
					.build();
		}
		throw RestCallException.newException(response);
	}

	private static <T> HttpResponse<T> getBadHttpResponse(final Response<?> response, Class<T> expectedResponseType) {
		if (!response.isSuccessful()) {
			return HttpResponse.<T>builder()
					.isSuccessful(response.isSuccessful())
					.statusCode(response.code())
					.body(getErrorBody(response, expectedResponseType))
					.build();
		}
		throw RestCallException.newException(response);
	}

	public static <T> T getErrorBody(final Response<?> response, Class<T> errorBodyType) {
		if (!response.isSuccessful() && response.errorBody() != null) {
			try {
				return GSON.fromJson(response.errorBody().string(), errorBodyType);
			} catch (IOException e) {
				throw RestCallException.newException(e);
			}
		}
		throw RestCallException.newException(response);
	}

	private static <T> T getResponseBody(final Response<T> response) {
		if (response.isSuccessful()) {
			return response.body();
		}
		throw RestCallException.newException(response);
	}

	private static <T> T request(final Call<?> apiCall, Class<T> responseType, BiFunction<Response<?>, Class<T>, T> responseCallback) {
		final Response<?> response;
		try {
			response = apiCall.execute();
			return responseCallback.apply(response, responseType);
		} catch (IOException e) {
			throw RestCallException.newException(e);
		}
	}

	private static <T, U> U requestHttpResponse(final Call<T> apiCall, Class<T> responseType,
	                                            final BiFunction<Response<T>, Class<T>, U> responseCallback) {
		final Response<T> response;
		try {
			//TODO addToCollection true
			response = executeRequest(apiCall, true);
			return responseCallback.apply(response, responseType);
		} catch (IOException e) {
			throw RestCallException.newException(e);
		}
	}

	private static <T, U> U request(final Call<T> apiCall, final Function<Response<T>, U> responseCallback, final boolean addToCollection) {
		final Response<T> response;
		try {
			response = executeRequest(apiCall, addToCollection);
			return responseCallback.apply(response);
		} catch (IOException e) {
			throw RestCallException.newException(e);
		}
	}

	private static <T> Response<T> executeRequest(final Call<T> apiCall, final boolean addToCollection) throws IOException {
		try {
			if (addToCollection) {
				PostmanRetrofitInterceptor.enable();
			}
			return apiCall.execute();
		} finally {
			if (addToCollection) {
				PostmanRetrofitInterceptor.disable();
			}
		}
	}
}
