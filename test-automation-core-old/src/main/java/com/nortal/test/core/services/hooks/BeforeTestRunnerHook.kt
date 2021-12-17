package com.nortal.test.core.services.hooks

/**
 * Interface for performing before test runner preparations.
 * Will execute before each test runner - ParallelTestRunner, SequentialTestRunner, SelectiveParallelTestRunner
 */
interface BeforeTestRunnerHook {
    /**
     * Returns order in which the hooks will be run. Lower order runs first.
     * Default order: 10000
     *
     * @return order
     */
    fun beforeTestRunnerOrder(): Int {
        return DEFAULT_ORDER
    }

    /**
     * Prepare for the test suite.
     */
    fun beforeTestRunner(hookContext: HookContext?)

    companion object {
        const val DEFAULT_ORDER = 10000
    }
}