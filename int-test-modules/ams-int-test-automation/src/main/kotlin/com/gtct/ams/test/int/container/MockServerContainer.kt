package com.nortal.test.container

import com.nortal.test.services.testcontainers.ContextContainer
import org.mockserver.client.MockServerClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

@Component
@ConditionalOnProperty(value = ["test-automation.containers.mock-server.enabled"], havingValue = "true")
class MockServerContainer(
    @Value("test-automation.containers.mock-server.aliases") private val aliases: Array<String>
) : ContextContainer {
  private val mockServer: MockServerContainer = configure()
  private lateinit var mockServerClient: MockServerClient

  override fun start(network: Network?) {
    mockServer
        .withNetworkAliases(*aliases)
        .withNetwork(network)
        .start()

    mockServerClient = createClient()
  }

  fun createClient(): MockServerClient {
    return MockServerClient(mockServer.host, mockServer.getMappedPort(MockServerContainer.PORT))
  }

  fun getClient(): MockServerClient {
    return mockServerClient;
  }

  fun resetMockClient() {
    mockServerClient.reset()
  }

  /**
   * Get internal endpoint for configured service.
   * @param index corresponds to alias in configuration
   */
  fun getInternalEndpoint(index: Int): String? {
    return String.format("http://%s:%d", aliases[index], MockServerContainer.PORT)
  }

  private fun configure(): MockServerContainer {
    return MockServerContainer(DockerImageName.parse("mockserver/mockserver"))
  }
}