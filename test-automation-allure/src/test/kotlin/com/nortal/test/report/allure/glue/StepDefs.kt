package com.nortal.test.report.allure.glue

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