package com.nortal.test.core.plugin;

import com.nortal.test.postman.model.PostmanFeatureInfo;
import io.cucumber.messages.Messages;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CucumberScenarioNameProvider {
	private static final CucumberScenarioNameProvider INSTANCE = new CucumberScenarioNameProvider();

	private final ThreadLocal<PostmanFeatureInfo> featureInfoThreadLocal = new ThreadLocal<>();

	public static CucumberScenarioNameProvider getInstance() {
		return INSTANCE;
	}

	public PostmanFeatureInfo getFeatureInfo() {
		return featureInfoThreadLocal.get();
	}

	public void setFeatureInfo(final Messages.GherkinDocument.Feature feature, final String featurePath) {
		if (feature == null) {
			log.warn("Returned feature was null. Scenario name was not found.");
			return;
		}

		PostmanFeatureInfo featureInfo = PostmanFeatureInfo.builder()
				.featureId(createFeatureId(featurePath))
				.featureTitle(feature.getName())
				.featureDescription(feature.getDescription())
				.build();

		this.featureInfoThreadLocal.set(featureInfo);
	}

	private String createFeatureId(final String featurePath) {
		return featurePath.replaceAll("[^a-zA-Z0-9_]+", "_");
	}

	public void clearCurrentFeatureInfo() {
		this.featureInfoThreadLocal.remove();
	}

}
