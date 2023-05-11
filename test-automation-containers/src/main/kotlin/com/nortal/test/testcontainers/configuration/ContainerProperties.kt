/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
     * container alias that will be registered under its network. This is also used for container name.
     */
    var internalNetworkAlias: String = "container-under-test",

    /**
     * Application startup timeout.
     */
    var startupTimeout: Long = 120,

    /**
     * Reuse testable container between runs. It will be redeployed if container jar war rebuilt. Eventually container has to be manually closed.
    NOTE: must be disabled for shared docker instances like CI runners.
     */
    val reuseBetweenRuns: Boolean = false,
    /**
     * Spring Boot testable container configuration.
     */
    @NestedConfigurationProperty
    val springBoot: SpringBootTestContainerProperties = SpringBootTestContainerProperties(),
)

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.containers.testable-container.spring-boot")
class SpringBootTestContainerProperties(
    /**
     * Base image for docker container.
     */
    val baseImage: String = "azul/zulu-openjdk:11",
    /**
     * directory where testable jar will be created. Picks first jar in dir.
     */
    val jarBuildDir: String = "",
    /**
     * jar matcher regex pattern. Should be changes if build generates more than one jar.
     */
    val jarRegexMatcher: String = "^.+?.*(?<!plain)\\.jar\$",

    /**
     * If enabled exposes [debugPort] as remote debug port.
     */
    val jarDebugEnabled: Boolean = true,
    /**
     * Debug port for container debugging
     */
    var debugPort: Int = 9000,
    /**
     * memory settings that will be applied on container
     */
    val memorySettings: String = "-Xmx512m",
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
    @NestedConfigurationProperty
    val jacoco: TestableContainerJacocoProperties = TestableContainerJacocoProperties(),
)

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.containers.testable-container.spring-boot.jacoco")
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

