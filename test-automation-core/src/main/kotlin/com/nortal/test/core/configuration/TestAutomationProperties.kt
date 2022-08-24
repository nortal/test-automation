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
package com.nortal.test.core.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

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
    /**
     * A name that is used in published reports.
     */
    val reportName: String = "test-report",

    /**
     * Additional spring component scan for beans that should be loaded together with the framework.
     */
    val springComponentScan: String = "com.nortal.test",
    /**
     * Cucumber configuration.
     */
    @NestedConfigurationProperty
    val cucumber: TestAutomationCucumberProperties = TestAutomationCucumberProperties(),
)

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.cucumber")
class TestAutomationCucumberProperties(
    /**
     * Property name to set the glue path: {@value}
     * <p>
     * A comma separated list of a classpath uri or package name e.g.:
     * {@code com.example.app.steps}.
     *
     * @see io.cucumber.core.feature.GluePath
     */
    val glueAppend: String = "",
)

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.cucumber.filter")
class TestAutomationCucumberFilterProperties(
    /**
     * Property name used to set name filter: "cucumber.filter.name"
     * Filters scenarios by name based on the provided regex pattern e.g: ^Hello (World|Cucumber)$.
     * Scenarios that do not match the expression are not executed.
     * By default all scenarios are executed
     */
    val name: String = "",
    /**
     * Property name used to set tag filter: "cucumber.filter.tags"
     * Filters scenarios by tag based on the provided tag expression e.g: @Cucumber and not (@Gherkin or @Zucchini).
     * Scenarios that do not match the expression are not executed.
     * By default all scenarios are executed
     */
    val tags: String = "",
)

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.cucumber.execution")
class TestAutomationCucumberExecutionProperties(
    /**
     * Property name used to enable dry-run: {@value}
     * <p>
     * When using dry run Cucumber will skip execution of glue code.
     * <p>
     * By default, dry-run is disabled
     */
    val dryRun: Boolean = false,
    /**
     * Property name used to set execution order: {@value}
     * <p>
     * Valid values are {@code lexical}, {@code reverse}, {@code random} or
     * {@code random:[seed]}.
     * <p>
     * By default features are executed in lexical file name order
     */
    val order: String = "",
)

@ConstructorBinding
@Suppress("UnusedPrivateMember")
@ConfigurationProperties(prefix = "test-automation.cucumber.execution.parallel")
class TestAutomationCucumberExecutionParallelProperties(
    /**
     * Enable parallel scenario execution
     */
    val enabled: Boolean = false,
    /**
     * Parallel executor count. Should be fine-tuned for execution env.
     */
    val executorCount: Int = 1,
    /**
     * Comma separated list for execution groups.
     * Tests with these tags will be grouped before execution and tests within same group will not overlap with each other.
     */
    executionGroupTags: String = "",
    /**
     * Tests tagged with this tag would always be executed separately from other tests.
     */
    isolationTag: String = ""
)
