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
package com.nortal.test.testcontainers.hook

import com.nortal.test.core.exception.TestAutomationException
import com.nortal.test.core.services.ScenarioExecutionContext
import com.nortal.test.core.services.hooks.BeforeScenarioHook
import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.testcontainers.TestContainerService
import com.nortal.test.testcontainers.TestContextContainerService
import com.nortal.test.testcontainers.TestableContainerInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ContainerHealthCheckHook(
    private val testContextContainerService: TestContextContainerService,
    private val testableContainerInitializer: TestableContainerInitializer
) : BeforeSuiteHook, BeforeScenarioHook {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun beforeSuiteOrder(): Int {
        return BeforeSuiteHook.DEFAULT_ORDER - 100
    }

    override fun beforeSuite() {
        try {
            testContextContainerService.startContext()
            testableContainerInitializer.initialize()
        } catch (exception: Exception) {
            log.error("Container startup has failed.", exception)
            throw TestAutomationException("Container startup has failed.", exception)
        }
    }

    override fun before(scenario: ScenarioExecutionContext) {
        if (TestContainerService.INIT_FAILED.get()) {
            throw TestAutomationException("Stopping scenario execution as test container is in failed state")
        }
    }

    override fun beforeScenarioOrder(): Int {
        return 0
    }
}