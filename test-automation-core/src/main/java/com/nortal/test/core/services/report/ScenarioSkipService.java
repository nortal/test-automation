package com.nortal.test.core.services.report;

import com.nortal.test.core.services.report.cucumber.SkippedScenarios;
import com.nortal.test.core.model.Tags;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks skipped scenarios so that they could be added to the report later on.
 */
@Component
public class ScenarioSkipService {

	private final Map<String, List<PickleWrapper>> skippedScenariosByFeatureName = new HashMap<>();

	public void collectSkippedScenarios(final FeatureWrapper feature, final PickleWrapper pickle) {
		if(feature == null || pickle == null) {
			return;
		}

		final List<String> tags = pickle.getPickle().getTags();
		if (tags.stream().noneMatch(Tags.SKIP.getName()::equals)) {
			return;
		}

		// FeatureWrapperImpl wraps the name of the feature in double quotes.
		// These get in the way later so we remove them.
		String featureName = RegExUtils.removePattern(String.valueOf(feature), "^\"|\"$");
		skippedScenariosByFeatureName.compute(featureName, (name, pickleWrappers) -> {
			List<PickleWrapper> skippedPickles = new ArrayList<>();
			if(pickleWrappers != null) {
				skippedPickles = pickleWrappers;
			}

			if (isNotPresent(pickle, skippedPickles)) {
				skippedPickles.add(pickle);
			}

			return skippedPickles;
		});
	}

	private boolean isNotPresent(final PickleWrapper pickle, final List<PickleWrapper> skippedPickles) {
		final String pickleName = pickle.getPickle().getName();
		return skippedPickles.stream()
				.map(PickleWrapper::getPickle)
				.map(Pickle::getName)
				.noneMatch(pickleName::equals);
	}

	public SkippedScenarios getSkippedScenarios() {
		return new SkippedScenarios(skippedScenariosByFeatureName);
	}
}
