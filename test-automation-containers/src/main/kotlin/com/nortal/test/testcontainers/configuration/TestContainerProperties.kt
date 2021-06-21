package com.nortal.test.testcontainers.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.containers")
data class TestContainerProperties(
    /**
     * Configuration for testable container.
     */
    val testableContainer: TestableContainerProperties
)

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.containers.testable-container")
class TestableContainerProperties(
    /**
     * Base image for docker container.
     */
    val baseImage: String = "openjdk:11-slim",
    /**
     * directory where testable jar will be created. Picks first jar in dir.
     */
    val jarBuildDir: String,
    /**
     * jar matcher regex pattern. Should be changes if build generates more than one jar.
     */
    val jarRegexMatcher: String = "^.+-.+.*(?<!plain)\\.jar\$",
    /**
     * Debug port for container debugging
     */
    var debugPort: Int = 9000,
    /**
     * Should container wait before debugger is attached?
     */
    val waitForDebugger: Boolean = false,
    /**
     * Spring profiles that will be activated for target application.
     */
    val springProfilesToActivate: String = "cucumber",
    /**
     * Jacoco test coverage configuration.
     */
    val jacoco: TestableContainerJacocoProperties,
)

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.containers.testable-container.jacoco")
class TestableContainerJacocoProperties(
    /**
     * Control jacoco execution during test run.
     */
    val enabled: Boolean = false,
    /**
     * TCP communication port.
     */
    val port: Int = 3600,
)
