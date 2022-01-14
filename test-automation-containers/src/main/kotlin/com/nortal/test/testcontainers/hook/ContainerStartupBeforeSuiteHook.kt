package com.nortal.test.testcontainers.hook

import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.testcontainers.TestContextContainerService
import com.nortal.test.testcontainers.TestableContainerInitializer
import org.springframework.stereotype.Component

@Component
class ContainerStartupBeforeSuiteHook(
    private val testContextContainerService: TestContextContainerService,
    private val testableContainerInitializer: TestableContainerInitializer
) : BeforeSuiteHook {

    override fun beforeSuiteOrder(): Int {
        return BeforeSuiteHook.DEFAULT_ORDER - 100
    }

    override fun beforeSuite() {
        testContextContainerService.startContext()
        testableContainerInitializer.initialize()
    }
}