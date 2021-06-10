package com.nortal.test.container

import com.nortal.test.core.services.ScenarioContainer
import com.nortal.test.core.services.hooks.BeforeScenarioHook
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["test-automation.containers.mock-server.enabled"], havingValue = "true")
class ScenarioMockResetHook(private val mockServerContainer: MockServerContainer) : BeforeScenarioHook {

  override fun before(scenario: ScenarioContainer?) {
    mockServerContainer.resetMockClient()
  }

}
