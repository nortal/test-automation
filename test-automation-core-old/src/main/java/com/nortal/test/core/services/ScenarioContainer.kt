package com.nortal.test.core.services

import com.nortal.test.core.model.ScenarioContext
import io.cucumber.java.Scenario
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component
import java.lang.ThreadLocal

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
@Slf4j
@Component
class ScenarioContainer {
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