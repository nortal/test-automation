package com.nortal.test.core.services.hooks

import com.nortal.test.core.services.ScenarioExecutionContext

/**
 * Interface for performing before scenario preparations. Will run in @Before tag of cucumber.
 */
interface BeforeScenarioHook {
    /**
     * Runs pre scenario preparation operations.
     */
    fun before(scenario: ScenarioExecutionContext)
}