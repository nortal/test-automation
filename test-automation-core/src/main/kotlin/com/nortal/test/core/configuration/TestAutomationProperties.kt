package com.nortal.test.core.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation")
data class TestAutomationProperties(

    val environment: String = "none",
)