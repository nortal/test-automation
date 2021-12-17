package io.cucumber.testng

import io.cucumber.core.gherkin.Feature
import io.cucumber.testng.FeatureWrapper
import lombok.Getter

/**
 * Direct copy from the lib. Changing class visibility.
 */
@Getter
class FeatureWrapperImpl internal constructor(val feature: Feature) : FeatureWrapper {
    override fun toString(): String {
        return "\"" + feature.name + "\""
    }
}