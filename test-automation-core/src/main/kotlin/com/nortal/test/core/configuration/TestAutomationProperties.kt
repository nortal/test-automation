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
    val name: String?,
    /**
     * Property name used to set tag filter: "cucumber.filter.tags"
     * Filters scenarios by tag based on the provided tag expression e.g: @Cucumber and not (@Gherkin or @Zucchini).
     * Scenarios that do not match the expression are not executed.
     * By default all scenarios are executed
     */
    val tags: String?,
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
    val dryRun: Boolean?,
    /**
     * Property name used to set execution order: {@value}
     * <p>
     * Valid values are {@code lexical}, {@code reverse}, {@code random} or
     * {@code random:[seed]}.
     * <p>
     * By default features are executed in lexical file name order
     */
    val order: String?,
)

@ConstructorBinding
@Suppress("UnusedPrivateMember")
@ConfigurationProperties(prefix = "test-automation.cucumber.execution.parallel")
class TestAutomationCucumberExecutionParallelProperties(
    /**
     * Enable parallel scenario execution
     */
    val enabled: Boolean,
    /**
     * Parallel executor count. Should be fine-tuned for execution env.
     */
    val executorCount: Int,
    /**
     * Comma separated list for execution groups.
     * Tests with these tags will be grouped before execution and tests within same group will not overlap with each other.
     */
    executionGroupTags: String?,
    /**
     * Tests tagged with this tag would always be executed separately from other tests.
     */
    isolationTag: String?
)
