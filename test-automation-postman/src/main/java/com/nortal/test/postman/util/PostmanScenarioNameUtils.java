package com.nortal.test.postman.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostmanScenarioNameUtils {
	private static final String SEPARATOR = "~";

	public static String createKey(final String scenarioId, final String scenarioName) {
		return scenarioId + SEPARATOR + scenarioName;
	}

	public static String getScenarioName(final String key) {
		return key.split(SEPARATOR)[1];
	}
}
