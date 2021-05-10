package com.nortal.test.core.services.report.cucumber;

import com.google.common.html.HtmlEscapers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;

public final class JsonFormattingUtils {

	private static final Gson GSON = new GsonBuilder()
			.serializeNulls()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();

	private JsonFormattingUtils() {
		//static utils
	}

	public static String prettyPrintJson(final String body) {
		try {
			if (StringUtils.isBlank(body)) {
				return body;
			}
			//Since GSON defaults all unknown number types to Double it does not work well when trying to pretty print
			//as it shows numbers in 2.14124E14 form. Thats why we use a regular JSONObject here.
			return HtmlEscapers.htmlEscaper().escape(new JSONObject(body).toString(4));
		} catch (RuntimeException | JSONException e) {
			throw new AssertionError("Failed to prettify body", e);
		}
	}

	public static String prettyPrintJson(final Object body) {
		try {
			return HtmlEscapers.htmlEscaper().escape(GSON.toJson(body));
		} catch (RuntimeException e) {
			throw new AssertionError("Failed to prettify body", e);
		}
	}

	public static String prettyPrintHtmlJson(final Object body) {
		return "<div class=\"json-view\" data-input-json=\"" + Base64.getEncoder().encodeToString(GSON.toJson(body).getBytes()) + "\"/>";
	}

	public static String prettyPrintHtml(final Object body) {
		return "<code class=\"json\">" + prettyPrintJson(body) + "</code>";
	}
}
