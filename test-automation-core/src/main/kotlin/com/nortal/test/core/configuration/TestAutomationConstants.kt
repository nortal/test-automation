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

object TestAutomationConstants {
    const val SPRING_PROFILE_CORE = "core"
    const val SPRING_PROFILE_BASE = "base"
    const val SPRING_PROFILE_OVERRIDE = "override"
    const val SPRING_PROFILE_CI = "ci"

    val ALL_SPRING_PROFILES =
        listOf(SPRING_PROFILE_CORE, SPRING_PROFILE_BASE, SPRING_PROFILE_OVERRIDE, SPRING_PROFILE_CI, SPRING_PROFILE_CI)

    const val FRAMEWORK_BOOTSTRAP_GLUE = "com.nortal.test.core.cucumber.glue"
    const val PROPERTY_BOOTSTRAP_GLUE_APPEND = "test-automation.cucumber.glue-append"


    const val PARALLEL_CONFIG_STRATEGY_PROPERTY = "fixed"
    const val PROPERTY_PARALLEL_EXECUTOR_COUNT = "test-automation.cucumber.execution.parallel.executor-count"
    const val PROPERTY_PARALLEL_EXECUTION_GROUP_TAGS =
        "test-automation.cucumber.execution.parallel.execution-group-tags"
    const val PROPERTY_PARALLEL_ISOLATION_TAG = "test-automation.cucumber.execution.parallel.isolation-tag"
}