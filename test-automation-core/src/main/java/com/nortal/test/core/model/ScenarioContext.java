package com.nortal.test.core.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.nortal.test.core.rest.HttpResponse;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ScenarioContext {
	private final String scenarioId = UUID.randomUUID().toString();
	private final OffsetDateTime scenarioStartTime = OffsetDateTime.now();

	private final ScenarioCommonContext common = new ScenarioCommonContext();

	@Data
	public static class ScenarioCommonContext {
		private HttpResponse<?> lastHttpResponse;

		private final Map<String, String> headers = new HashMap<>();

		public void addHeader(final String key, final String value) {
			headers.put(key, value);
		}
	}


}
