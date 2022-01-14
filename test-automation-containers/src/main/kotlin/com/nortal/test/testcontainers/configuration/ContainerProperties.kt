package com.nortal.test.testcontainers.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.containers")
data class ContainerProperties(
    /**
     * Enables use of default Docker bridge network.
     */
    val useDefaultBridgeNetwork: Boolean = false,
    /**
     * Container under testing configuration.
     */
    val testableContainer: TestableContainerProperties,
    /**
     * Context containers that are required for testable container to properly work.
     */
    val contextContainers: Map<String, ContextContainerProperties> = hashMapOf()
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
    val jarBuildDir: String = "",
    /**
     * jar matcher regex pattern. Should be changes if build generates more than one jar.
     */
    val jarRegexMatcher: String = "^.+-.+.*(?<!plain)\\.jar\$",

    /**
     * If enabled exposes [debugPort] as remote debug port.
     */
    val jarDebugEnabled: Boolean = true,
    /**
     * Debug port for container debugging
     */
    var debugPort: Int = 9000,
    /**
     * Internal Http port that has to be exposed
     */
    var internalHttpPort: Int = 8080,
    /**
     * Application startup timeout.
     */
    var startupTimeout: Long = 120,
    /**
     * Should container wait before debugger is attached?
     */
    val waitForDebugger: Boolean = false,
    /**
     * Spring profiles that will be activated for target application.
     */
    val springProfilesToActivate: String = "cucumber",
    /**
     * Reuse testable container between runs. It will be redeployed if container jar war rebuilt. Eventually container has to be manually closed.
    NOTE: must be disabled for shared docker instances like CI runners.
     */
    val reuseBetweenRuns: Boolean = false,
    /**
     * Jacoco test coverage configuration.
     */
    @NestedConfigurationProperty
    val jacoco: TestableContainerJacocoProperties = TestableContainerJacocoProperties(),
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
    /**
     * TCP communication host.
     */
    val host: String = "localhost",
    /**
     * Root destination dir for jacoco content.
     */
    val destDir: String = "build/jacoco/",
    /**
     * HTML report directory.
     */
    val destReportDir: String = "build/reports/system-tests",
    /**
     * Regex to find java classes.
     */
    val structureAnalysisRegex: String = ".+build.classes.+[\\\\,/]main",
    /**
     * Regex to find java/kotlin source.
     */
    val sourceCodeLookupRegex: String = ".+src[\\\\,/]main[\\\\,/](kotlin|java)"
)

@ConstructorBinding
class ContextContainerProperties(
    /**
     * If this container is enabled
     */
    val enabled: Boolean = false,
    /**
     * Reuse container between runs. Eventually container has to be manually closed.
     * Feature is mainly controller by test-automation.containers.testable-container.reuse-between-runs,
     * if that is disabled - this does nothing.
     */
    val reuseBetweenRuns: Boolean = true
)

