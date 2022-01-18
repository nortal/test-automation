package com.nortal.test.testcontainers.hook

import com.nortal.test.core.exception.TestAutomationException
import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.testcontainers.TestContextContainerService
import com.nortal.test.testcontainers.TestableContainerInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ContainerStartupBeforeSuiteHook(
    private val testContextContainerService: TestContextContainerService,
    private val testableContainerInitializer: TestableContainerInitializer
) : BeforeSuiteHook {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun beforeSuiteOrder(): Int {
        return BeforeSuiteHook.DEFAULT_ORDER - 100
    }

    override fun beforeSuite() {
        try {
            testContextContainerService.startContext()
            testableContainerInitializer.initialize()
        } catch (exception: Exception) {
            log.error("Container startup has failed.", exception)
            throw TestAutomationException("Container startup has failed.", exception)
        }
    }
}