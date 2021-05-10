package com.nortal.test.core.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.nortal.test.postman.PostmanScenarioRequestContext;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class ScenarioContext implements PostmanScenarioRequestContext {
	private final String scenarioId = UUID.randomUUID().toString();
	private final OffsetDateTime scenarioStartTime = OffsetDateTime.now();

	private final ScenarioCommonContext common = new ScenarioCommonContext();
	private final SequentialScenarioContext sequential = new SequentialScenarioContext();
	private final ScenarioMockContext mock = new ScenarioMockContext();
	private ScenarioAuthContext authentication = new ScenarioAuthContext();
	private final QueryCache queryCache = new QueryCache();

	@Override
	public String getJwtToken() {
		return getAuthentication().getJwtToken();
	}

	@Data
	public static class ScenarioCommonContext {
		private HttpStatus lastHttpStatus;
		private final Map<String, String> headers = new HashMap<>();

		public void setHeaders(final String key, final String value) {
			headers.put(key, value);
		}
	}

	@Data
	public static class SequentialScenarioContext {
		private Map<String, Boolean> promotionsToggledMap;

		public Map<String, Boolean> getPromotionsToggledMap() {
			return Objects.requireNonNullElse(this.promotionsToggledMap, new HashMap<>());
		}
	}

	@Data
	public static class ScenarioMockContext {
		private String lastOmsMockId;
	}

	@Data
	public static class ScenarioAuthContext {

		private String jwtToken;

	}


	public void clearAuthenticationContext() {
		authentication = new ScenarioAuthContext();
	}

	public void updateAuthenticationContext(final ScenarioAuthContext newContext) {
		authentication = newContext;
	}
}
