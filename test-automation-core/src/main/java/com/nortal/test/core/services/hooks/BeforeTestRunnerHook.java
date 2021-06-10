package com.nortal.test.core.services.hooks;

/**
 * Interface for performing before test runner preparations.
 * Will execute before each test runner - ParallelTestRunner, SequentialTestRunner, SelectiveParallelTestRunner
 */
public interface BeforeTestRunnerHook {

	int DEFAULT_ORDER = 10000;

	/**
	 * Returns order in which the hooks will be run. Lower order runs first.
	 * Default order: 10000
	 *
	 * @return order
	 */
	default int beforeTestRunnerOrder() {
		return DEFAULT_ORDER;
	}

	/**
	 * Prepare for the test suite.
	 */
	void beforeTestRunner(HookContext hookContext);
}
