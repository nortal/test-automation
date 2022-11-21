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
package com.nortal.test.core.glue

import com.nortal.test.core.exception.TestAutomationException
import com.nortal.test.core.services.CucumberScenarioProvider
import com.nortal.test.core.services.ScenarioContext
import io.cucumber.java.en.Step
import org.junit.jupiter.api.Assertions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StepDefs(
    private val scenarioProvider: CucumberScenarioProvider,
    private val scenarioContext: ScenarioContext
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private var scenarioName: String? = null

    @Step("A step is called")
    fun `a step is called`() {
        log.info("A step is called within test {}", scenarioProvider.getCucumberScenario().name)
        scenarioName = scenarioProvider.getCucumberScenario().name
        scenarioContext.putStepData("key", scenarioProvider.getCucumberScenario().name)
        Thread.sleep(100L)
    }

    @Step("Something is called")
    fun `something is called`() {
        scenarioContext.getRequiredStepData("key") as String?
        log.info("A step is called within test {} val {}", scenarioProvider.getCucumberScenario().name, hashCode())

        //verify that parallel execution is not messing up values.
        Assertions.assertEquals(scenarioName, scenarioProvider.getCucumberScenario().name)
        Thread.sleep(50L)
    }

    @Step("Something is done")
    fun `something is done`() {
        Thread.sleep(50L)
    }

    @Step("A failing step")
    fun `a failing step`() {
        throw TestAutomationException("This should fail")
    }
}