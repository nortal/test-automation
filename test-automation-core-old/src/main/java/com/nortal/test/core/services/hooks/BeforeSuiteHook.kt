package com.nortal.test.core.services.hooks

/**
 * Interface for performing before test suite preparations.
 * Will only execute once before the first scenario.
 */
interface BeforeSuiteHook {
    /**
     * Returns order in which the hooks will be run. Lower order runs first.
     * Default order: 10000
     *
     * @return order
     */
    fun beforeSuiteOrder(): Int {
        return DEFAULT_ORDER
    }

    /**
     * Prepare for the test suite.
     */
    fun beforeSuite()

    companion object {
        const val DEFAULT_ORDER = 10000
    }
}