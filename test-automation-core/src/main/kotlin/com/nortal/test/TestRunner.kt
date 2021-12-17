package com.nortal.test

import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.ConfigurationParameters
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@ConfigurationParameters(
    ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = TestRunner.FRAMEWORK_BOOTSTRAP_GLUE),
    ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
)
abstract class TestRunner() {
    companion object {
        const val FRAMEWORK_BOOTSTRAP_GLUE = "com.nortal.test.core.cucumber.glue"

        init {
            System.setProperty("CUCUMBER_PUBLISH_ENABLED", "false")
        }
    }
}