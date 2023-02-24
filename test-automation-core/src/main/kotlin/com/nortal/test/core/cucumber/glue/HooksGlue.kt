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
package com.nortal.test.core.cucumber.glue

import com.nortal.test.core.services.ScenarioExecutionContext
import com.nortal.test.core.services.hooks.AfterScenarioHook
import com.nortal.test.core.services.hooks.BeforeScenarioHook
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HooksGlue(
    private val afterScenarioHooks: List<AfterScenarioHook>,
    private val beforeScenarioHooks: List<BeforeScenarioHook>,
    private val scenarioExecutionContext: ScenarioExecutionContext
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Cucumber does not expose a way to retrieve Scenario variable in the middle of a step, thus we must resort to grabbin it at the start and
     * keeping it.
     */
    @Before(order = -1)
    fun prepareScenarioContext(scenario: Scenario) {
        log.info("Preparing scenario container")
        scenarioExecutionContext.prepare(scenario)
    }

    @Before(order = 1)
    fun beforeScenario() {
        beforeScenarioHooks.stream()
            .sorted(Comparator.comparingInt { obj: BeforeScenarioHook -> obj.beforeScenarioOrder() })
            .forEach { it: BeforeScenarioHook -> it.before(scenarioExecutionContext) }
    }

    @After(order = Int.MIN_VALUE)
    fun afterScenario() {
        afterScenarioHooks.stream()
            .sorted(Comparator.comparingInt { obj: AfterScenarioHook -> obj.afterScenarioOrder() })
            .forEach { hook: AfterScenarioHook -> hook.after(scenarioExecutionContext) }
    }

}