package com.nortal.test.postman.generate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nortal.test.postman.PostmanScenarioRequestContext;
import com.nortal.test.postman.api.model.Body;
import com.nortal.test.postman.util.PostmanJsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import com.nortal.test.postman.api.model.Header;
import com.nortal.test.postman.api.model.Item;
import com.nortal.test.postman.api.model.Mode;
import com.nortal.test.postman.api.model.QueryParam;
import com.nortal.test.postman.api.model.Request;
import com.nortal.test.postman.api.model.Response;
import com.nortal.test.postman.api.model.URL;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;

@Slf4j
@AllArgsConstructor
@Component
public class OkHttpRequestRecorder {
	private final PostmanHeaderTransformer postmanHeaderFilter;
	private final PostmanPlaceholderInjector postmanPlaceholderInjector;

	public Item record(final PostmanScenarioRequestContext requestContext,
	                   final okhttp3.Request okHttpRequest,
	                   final okhttp3.Response response,
	                   final int currentStepCount) {
		final Item.ItemBuilder requestBuilder = Item.builder();

		final int thisStepNo = currentStepCount + 1;

		final Request request = createRequestUnion(requestContext, okHttpRequest);
		requestBuilder.id(UUID.randomUUID().toString());
		requestBuilder.name(getRequestName(okHttpRequest, request, thisStepNo));
		requestBuilder.request(request);
		requestBuilder.response(createResponse(response));

		return requestBuilder.build();
	}

	private String getRequestName(final okhttp3.Request okHttpRequest, final Request request, final int stepNo) {
		final String uri = StringUtils.join(request.getUrl().getPath(), "/");

		return String.format("%d. %s %s", stepNo, okHttpRequest.method().toUpperCase(), uri);
	}

	@SneakyThrows
	private Response createResponse(final okhttp3.Response response) {
		Response.ResponseBuilder responseBuilder = Response.builder()
				.name("Automation response.")
				.status(String.valueOf(response.code()));

		response.headers()
				.toMultimap()
				.forEach((headerKey, values) -> values
						.forEach(headerValue -> responseBuilder.header(postmanHeaderFilter.createHeader(headerKey, headerValue))));

		final ResponseBody body = response.peekBody(Long.MAX_VALUE);
		final String bodyStr = body.string();

		if (StringUtils.isNotBlank(bodyStr)) {
			try {
				responseBuilder.body(PostmanJsonUtils.prettifyJson(bodyStr));
			} catch (final Exception exception) {
				log.warn("Unable to capture response: " + bodyStr, exception);
			}
		}

		return responseBuilder.build();
	}

	private Request createRequestUnion(final PostmanScenarioRequestContext requestContext, final okhttp3.Request okHttpRequest) {
		final Request.RequestBuilder builder = Request.builder()
				.method(okHttpRequest.method())
				.url(createUrl(requestContext, okHttpRequest));

		okHttpRequest.headers()
				.toMultimap()
				.forEach((headerKey, values) -> values
						.forEach(headerValue -> builder.header(postmanHeaderFilter.createHeader(headerKey, headerValue))));

		if (okHttpRequest.body() != null) {
			final MediaType mediaType = okHttpRequest.body().contentType();
			if (mediaType != null) {
				builder.header(Header.builder()
						               .key("content-type")
						               .value(mediaType.toString())
						               .build());
			}

			final String json = convertRequestBodyToString(okHttpRequest);
			builder.body(Body.builder()
					             .mode(Mode.RAW)
					             .raw(PostmanJsonUtils.prettifyJson(json))
					             .build());
		}

		return builder.build();
	}

	private String convertRequestBodyToString(final okhttp3.Request request) {
		try (final Buffer buffer = new Buffer()) {
			final okhttp3.Request copy = request.newBuilder().build();

			if (copy.body() != null) {
				copy.body().writeTo(buffer);
			}

			return buffer.readUtf8();
		} catch (final IOException e) {
			log.error("Failed to read body", e);
			return null;
		}
	}

	private URL createUrl(final PostmanScenarioRequestContext requestContext, final okhttp3.Request okHttpRequest) {
		final URL url = URL.builder()
				.raw(okHttpRequest.url().toString())
				.host(getHost(okHttpRequest))
				.path(okHttpRequest.url().pathSegments())
				.queries(getQueryParameters(okHttpRequest))
				.build();

		postmanPlaceholderInjector.injectPlaceholdersAndScriptIfNecessary(requestContext, url);
		return url;
	}

	private String getHost(final okhttp3.Request okHttpRequest) {
		return String.format("%s://%s", getProtocol(okHttpRequest), okHttpRequest.url().host());
	}

	private String getProtocol(final okhttp3.Request okHttpRequest) {
		return okHttpRequest.isHttps() ? "https" : "http";
	}

	private List<QueryParam> getQueryParameters(final okhttp3.Request okHttpRequest) {
		final List<QueryParam> queryParams = new ArrayList<>();
		for (int index = 0; index < okHttpRequest.url().querySize(); index++) {
			queryParams.add(QueryParam.builder()
					                .disabled(false)
					                .key(okHttpRequest.url().queryParameterName(index))
					                .value(okHttpRequest.url().queryParameterValue(index))
					                .build());
		}
		return queryParams;
	}
}
