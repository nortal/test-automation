package com.nortal.test.core.plugin

import io.cucumber.messages.Messages.GherkinDocument
import lombok.extern.slf4j.Slf4j

@Slf4j
class CucumberScenarioNameProvider {
    fun setFeatureInfo(feature: GherkinDocument.Feature?, featurePath: String?) {
        if (feature == null) {
            CucumberScenarioNameProvider.log.warn("Returned feature was null. Scenario name was not found.")
            return
        }
        /*
		PostmanFeatureInfo featureInfo = PostmanFeatureInfo.builder()
				.featureId(createFeatureId(featurePath))
				.featureTitle(feature.getName())
				.featureDescription(feature.getDescription())
				.build();

		this.featureInfoThreadLocal.set(featureInfo);*/
    }

    private fun createFeatureId(featurePath: String): String {
        return featurePath.replace("[^a-zA-Z0-9_]+".toRegex(), "_")
    }

    fun clearCurrentFeatureInfo() {
//		this.featureInfoThreadLocal.remove();
    }

    companion object {
        val instance = CucumberScenarioNameProvider()
    }
}