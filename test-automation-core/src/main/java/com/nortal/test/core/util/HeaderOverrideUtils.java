package com.nortal.test.core.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderOverrideUtils {
	private static final Gson GSON = new Gson();

	public static final String PROPERTY_OVERRIDE_HEADER = "x-property-override";
	public static final String TSETTING_OVERRIDE_HEADER = "x-tsetting-override";

	public static <T> String objToBase64(final T prop) {
		final byte[] bytes = GSON.toJson(prop).getBytes(StandardCharsets.UTF_8);
		return Base64.getEncoder().encodeToString(bytes);
	}
}
