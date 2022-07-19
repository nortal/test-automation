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
package com.nortal.test.report.reportportal.glue

import com.nortal.test.core.exception.TestAutomationException
import com.nortal.test.core.services.ScenarioExecutionContext
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StepDefs(private val scenarioExecutionContext: ScenarioExecutionContext) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Given("A step is called")
    fun `a step is called`() {
        for (i in 1..500) {
            log.info("A step is called within test {}. Iteration $i", scenarioExecutionContext.getScenario().name)
        }

        Thread.sleep(50L)
    }

    @Then("Something is called")
    fun `something is called`() {
        Thread.sleep(50L)
    }

    @Then("Something is done")
    fun `something is done`() {
        Thread.sleep(50L)
    }

    @Then("A failing step")
    fun `a failing step`() {
        throw TestAutomationException("This should fail")
    }
}