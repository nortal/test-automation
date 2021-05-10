package com.nortal.test.core.services.report.cucumber;

import static com.nortal.test.core.services.report.cucumber.JsonFormattingUtils.prettyPrintHtmlJson;
import static com.nortal.test.core.services.report.cucumber.JsonFormattingUtils.prettyPrintJson;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.nortal.test.core.rest.RetrofitCallHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestResponseReportFormatter {
	private static final Gson GSON = new GsonBuilder()
			.serializeNulls()
			.setPrettyPrinting()
			.create();

	private final ReportFormatter formatter;

	/**
	 * Formats the request and response and adds it to the report.
	 *
	 * @param requestSpec request
	 * @param response    response
	 */
	public void formatAndAddToReport(final Request requestSpec, final Response response) {
		final ReportFormatter.Attachment attachment = populateRequestSection(ReportFormatter.Attachment.create(), requestSpec)
				.addSection("Response:", ReportFormatter.SectionType.COLLAPSIBLE, getResponse(response))
				/*.addSection("Registered header overrides:", ReportFormatter.SectionType.COLLAPSIBLE, getHeaderOverrides())*/;
		formatter.formatAndAddToReport(attachment);
	}

	public void formatAndAddToReport(final Request requestSpec) {
		final ReportFormatter.Attachment attachment = populateRequestSection(ReportFormatter.Attachment.create(), requestSpec);
		formatter.formatAndAddToReport(attachment);
	}

	private ReportFormatter.Attachment populateRequestSection(final ReportFormatter.Attachment attachment,
	                                                          final Request requestSpec) {
		attachment
				.setName(requestSpec.method().toUpperCase(Locale.ENGLISH) + " " + requestSpec.url().toString())
				.setTitle(getRequestDescription(requestSpec))
				.addSection("Request:", ReportFormatter.SectionType.STANDARD, getRequest(requestSpec));

		return attachment;
	}

	private String getRequest(final Request request) {
		final StringBuilder curl = new StringBuilder("curl -X ")
				.append(request.method().toUpperCase(Locale.ENGLISH))
				.append(" \\\n'")
				.append(request.url().toString())
				.append("' \\\n");

		request.headers()
				.toMultimap()
				.forEach((headerKey, values) -> values.forEach(headerValue -> appendHeader(curl, headerKey, headerValue)));

		if (request.body() != null) {

			final MediaType mediaType = request.body().contentType();
			if (mediaType != null) {
				appendHeader(curl, "content-type", mediaType.toString());
			}

			final String json = prettyPrintJson(RetrofitCallHelper.convertRequestBodyToString(request));
			curl.append("-d '").append(json).append('\'');
		}
		curl.append('\n');

		return curl.toString();
	}

	private void appendHeader(final StringBuilder curl, final String headerKey, final String headerValue) {
		curl.append("-H '").append(headerKey).append(": ").append(headerValue).append("' \\\n");
	}

	@SneakyThrows
	private String getResponse(final Response response) {
		final Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("status", response.code());
		responseMap.put("headers", response.headers().toMultimap());

		final ResponseBody body = response.peekBody(Long.MAX_VALUE);
		final String bodyStr = body.string();
		if (StringUtils.isNotBlank(bodyStr)) {
			try {
				final JsonElement element = GSON.fromJson(bodyStr, JsonElement.class);
				responseMap.put("body", element);
			} catch (JsonParseException ex) {
				responseMap.put("body", bodyStr);
			}
		}

		return prettyPrintHtmlJson(responseMap);
	}

	private String getRequestDescription(final Request requestSpec) {
		return String.format("Rest call: %s", requestSpec.url().toString());
	}

	/*private String getHeaderOverrides() {
		final EnvConfigurationOverride override = headerOverrideService.getHeaderConfigurationOverrides();
		return prettyPrintHtmlJson(override);
	}*/
}
