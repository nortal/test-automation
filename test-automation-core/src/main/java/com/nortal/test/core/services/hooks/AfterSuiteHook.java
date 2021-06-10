package com.nortal.test.core.services.hooks;

/**
 * Hook for running stuff after test suite finishes.
 */
public interface AfterSuiteHook {

	int DEFAULT_ORDER = 10000;

	/**
	 * Returns order in which the hooks will be run. Lower order runs first.
	 * Default order: 10000
	 *
	 * @return order
	 */
	default int afterSuitOrder() {
		return DEFAULT_ORDER;
	}

	/**
	 * This method is called on all components implementing it in a random order.
	 */
	void afterSuite();
}
