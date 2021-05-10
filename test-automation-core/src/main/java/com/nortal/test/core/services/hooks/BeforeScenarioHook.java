package com.nortal.test.core.services.hooks;

import com.nortal.test.core.services.ScenarioContainer;

/**
 * Interface for performing before scenario preparations. Will run in @Before tag of cucumber.
 */
public interface BeforeScenarioHook {
	/**
	 * Runs pre scenario preparation operations.
	 */
	void before(ScenarioContainer scenario);
}
