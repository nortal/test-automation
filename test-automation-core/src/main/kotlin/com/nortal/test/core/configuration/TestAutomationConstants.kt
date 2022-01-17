package com.nortal.test.core.configuration

object TestAutomationConstants {
    const val SPRING_PROFILE_CORE = "core"
    const val SPRING_PROFILE_BASE = "base"
    const val SPRING_PROFILE_OVERRIDE = "override"

    val ALL_SPRING_PROFILES = listOf(SPRING_PROFILE_CORE, SPRING_PROFILE_BASE, SPRING_PROFILE_OVERRIDE)

    const val FRAMEWORK_BOOTSTRAP_GLUE = "com.nortal.test.core.cucumber.glue"
    const val PROPERTY_BOOTSTRAP_GLUE_APPEND = "test-automation.cucumber.glue-append"


    const val PARALLEL_CONFIG_STRATEGY_PROPERTY = "fixed"
    const val PROPERTY_PARALLEL_EXECUTOR_COUNT = "test-automation.cucumber.execution.parallel.executor-count"
    const val PROPERTY_PARALLEL_EXECUTION_GROUP_TAGS = "test-automation.cucumber.execution.parallel.execution-group-tags"
    const val PROPERTY_PARALLEL_ISOLATION_TAG = "test-automation.cucumber.execution.parallel.isolation-tag"
}