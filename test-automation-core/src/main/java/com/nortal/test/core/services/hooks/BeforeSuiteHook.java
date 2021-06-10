package com.nortal.test.core.services.hooks;

/**
 * Interface for performing before test suite preparations.
 * Will only execute once before the first scenario.
 */
public interface BeforeSuiteHook {

	int DEFAULT_ORDER = 10000;

	/**
	 * Returns order in which the hooks will be run. Lower order runs first.
	 * Default order: 10000
	 *
	 * @return order
	 */
	default int beforeSuiteOrder() {
		return DEFAULT_ORDER;
	}

	/**
	 * Prepare for the test suite.
	 */
	void beforeSuite();
}
