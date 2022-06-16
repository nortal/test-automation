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
package com.nortal.test.core.services

import com.nortal.test.core.model.ScenarioContext
import io.cucumber.java.Scenario
import io.cucumber.spring.ScenarioScope
import org.springframework.stereotype.Component

/**
 * Container for the cucumber scenario object.
 *
 *
 * Cucumber framework is very stingy on allowing to access the scenario object
 * and only lets us grab it in before and after hooks. That's why we need a container component that would hold the reference during the actual
 * scenario steps.
 *
 *
 * Users beware that its possible that the container will be empty if it is accessed before scenario starts.
 */
@Component
@ScenarioScope //TODO remove thredlocal and use scenario scoped beans.
class ScenarioExecutionContext {
    private val scenario = ThreadLocal<Scenario>()
    private val scenarioContext = ThreadLocal<ScenarioContext>()

    fun prepare(scenario: Scenario) {
        this.scenario.set(scenario)
        scenarioContext.set(ScenarioContext())
    }

    fun clean() {
        scenario.remove()
        scenarioContext.remove()
    }

    fun getScenario(): Scenario {
        return scenario.get()
    }

    fun get(): ScenarioContext {
        return scenarioContext.get()
    }
}