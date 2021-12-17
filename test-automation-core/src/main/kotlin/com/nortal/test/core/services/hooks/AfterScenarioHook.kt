package com.nortal.test.core.services.hooks

import com.nortal.test.core.services.ScenarioExecutionContext

/**
 * Interface for performing after scenario cleanup. Any bean that implements this interface will have its cleanup method called during cucumber @After
 * tag.
 */
interface AfterScenarioHook {
    /**
     * Perform after scenario operations.
     */
    fun after(scenario: ScenarioExecutionContext?)
}