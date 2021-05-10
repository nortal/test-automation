package com.nortal.test.core.services.hooks;

import com.nortal.test.core.services.ScenarioContainer;

/**
 * Interface for performing after scenario cleanup. Any bean that implements this interface will have its cleanup method called during cucumber @After
 * tag.
 */
public interface AfterScenarioHook {
	/**
	 * Perform after scenario operations.
	 */
	void after(ScenarioContainer scenario);
}
