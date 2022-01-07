package com.nortal.test.core.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Root configuration class for test automation.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation")
data class TestAutomationProperties(
    /**
     * environment identifier.
     */
    val environment: String = "none",
)
