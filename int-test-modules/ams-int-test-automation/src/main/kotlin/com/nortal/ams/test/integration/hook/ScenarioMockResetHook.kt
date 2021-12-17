package com.nortal.ams.test.integration.hook

import com.nortal.ams.test.integration.container.MockServerContainer
import com.nortal.test.core.services.ScenarioExecutionContext
import com.nortal.test.core.services.hooks.BeforeScenarioHook
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["test-automation.containers.mock-server.enabled"], havingValue = "true")
class ScenarioMockResetHook(private val mockServerContainer: MockServerContainer) : BeforeScenarioHook {

  override fun before(scenario: ScenarioExecutionContext?) {
    mockServerContainer.resetMockClient()
  }

}
