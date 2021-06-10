package io.cucumber.testng;

import io.cucumber.core.gherkin.Feature;
import lombok.Getter;

/**
 * Direct copy from the lib. Changing class visibility.
 */
@Getter
public class FeatureWrapperImpl implements FeatureWrapper {
	private final Feature feature;

	FeatureWrapperImpl(Feature feature) {
		this.feature = feature;
	}

	public String toString() {
		return "\"" + this.feature.getName() + "\"";
	}
}
