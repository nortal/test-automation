package com.nortal.test.core.services

import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import java.util.*

@Component
class CucumberScopeMonitor(
    private val scenarioContextProvider: ObjectProvider<ScenarioContext>
) {

    /**
     * Sometimes it is necessary to detect whether bean is executing withing cucumber scope or before its creation.
     */
    @Suppress("SwallowedException")
    fun isCalledWithinScope(): Boolean {
        return try {
            scenarioContextProvider.getObject().scenarioId //see if we can access the bean
            true
        } catch (ex: Exception) {
            false
        }
    }
}