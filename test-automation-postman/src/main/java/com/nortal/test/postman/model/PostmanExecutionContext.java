package com.nortal.test.postman.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostmanExecutionContext {
	private String executionTitle;
	private String outputDir;
	private List<String> environments;
	private Map<String, String> collectionNames;

	private final PostmanScenarioContext scenarioContext = new PostmanScenarioContext();

	@Data
	public static class PostmanScenarioContext {
		private String jwtToken;
		private String cartId;
	}
}
