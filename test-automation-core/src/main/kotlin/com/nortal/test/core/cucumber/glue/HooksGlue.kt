package com.nortal.test.core.cucumber.glue

import com.nortal.test.core.services.ScenarioExecutionContext
import com.nortal.test.core.services.hooks.AfterScenarioHook
import com.nortal.test.core.services.hooks.BeforeScenarioHook
import com.nortal.test.core.services.hooks.BeforeSuiteHook
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer

class HooksGlue(
    private val afterScenarioHooks: List<AfterScenarioHook>,
    private val beforeScenarioHooks: List<BeforeScenarioHook>,
    private val suitePreparations: List<BeforeSuiteHook>,
    private val scenarioExecutionContext: ScenarioExecutionContext
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)


    companion object {
        private val initializationLock = ReentrantLock()

        @Volatile
        private var initialized = false
    }

    /**
     * Cucumber does not expose a way to retrieve Scenario variable in the middle of a step, thus we must resort to grabbin it at the start and
     * keeping it.
     */
    @Before(order = -1)
    fun prepareScenarioContext(scenario: Scenario) {
        log.info("Preparing scenario container")
        scenarioExecutionContext.prepare(scenario)
    }

    /**
     * This hooks sets up our environment. Since cucumber does not support BeforeSuite hooks we use a boolean to run it once only.
     */
    @Before(order = 0)
    fun configurationHook() {
        if (initialized) {
            return
        }

        try {
            initializationLock.lock()
            if (initialized) {
                return
            }
            initialized = true

            suitePreparations.stream()
                .sorted(Comparator.comparingInt { obj: BeforeSuiteHook -> obj.beforeSuiteOrder() })
                .peek { log.info("Running before suite hook for: {}", it.javaClass.name) }
                .forEach { obj: BeforeSuiteHook -> obj.beforeSuite() }
        } finally {
            initializationLock.unlock()
        }
    }

    @Before(order = 1)
    fun beforeScenario() {
        beforeScenarioHooks.forEach(Consumer { it: BeforeScenarioHook -> it.before(scenarioExecutionContext) })
    }

    @After(order = Int.MIN_VALUE)
    fun afterScenario() {
        afterScenarioHooks.forEach(Consumer { hook: AfterScenarioHook -> hook.after(scenarioExecutionContext) })
        log.info("Cleaning scenario container")
        scenarioExecutionContext.clean()
    }


}