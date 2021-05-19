package com.nortal.test.container

import com.nortal.test.services.testcontainers.ContextContainer
import com.nortal.test.services.testcontainers.images.builder.ReusableImageFromDockerfile
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import java.io.File
import java.time.Duration

@Component
@ConditionalOnProperty(value = ["test-automation.containers.postgre.enabled"], havingValue = "true")
class AmsPostgreContainer<SELF : GenericContainer<SELF>> : ContextContainer {
  val container: GenericContainer<SELF> = configure()

  companion object {
    private const val NETWORK_ALIAS = "postgres"
    private const val POSTGRE_INTERNAL_PORT = 5432
  }

  override fun start(network: Network?) {
    container.withNetwork(network)
        .start()
  }

  fun getJdbcUrl(): String {
    verifyRunning()
    return ("jdbc:postgresql://" + NETWORK_ALIAS + ":" + PostgreSQLContainer.POSTGRESQL_PORT
        + "/project_api_db")
  }

  private fun configure(): GenericContainer<SELF> {
    val imageFromDockerfile = ReusableImageFromDockerfile()
        .withDockerfile(File("../database/Docker/Dockerfile").toPath())

    return GenericContainer<SELF>(imageFromDockerfile)
        .withNetworkAliases(NETWORK_ALIAS)
        .withExposedPorts(POSTGRE_INTERNAL_PORT)
        .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
        .withEnv("POSTGRES_PASSWORD", "postgres")
        .withEnv("POSTGRES_USER", "postgres")
        .withStartupTimeout(Duration.ofMinutes(1))
  }

  private fun verifyRunning() {
    if (!container.isRunning) {
      throw AssertionError(
          "AMS postgre container is not started yet! Please start the context before trying to call this method."
      )
    }
  }
}
