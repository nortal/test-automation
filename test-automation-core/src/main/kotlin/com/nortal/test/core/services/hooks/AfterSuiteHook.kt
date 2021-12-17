package com.nortal.test.core.services.hooks

/**
 * Hook for running stuff after test suite finishes.
 */
interface AfterSuiteHook {
    /**
     * Returns order in which the hooks will be run. Lower order runs first.
     * Default order: 10000
     *
     * @return order
     */
    fun afterSuitOrder(): Int {
        return DEFAULT_ORDER
    }

    /**
     * This method is called on all components implementing it in a random order.
     */
    fun afterSuite()

    companion object {
        const val DEFAULT_ORDER = 10000
    }
}