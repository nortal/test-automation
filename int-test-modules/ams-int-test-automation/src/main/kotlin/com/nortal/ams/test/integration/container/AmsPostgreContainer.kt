package com.nortal.ams.test.integration.container

import com.nortal.test.jdbc.JdbcUrlProvider
import com.nortal.test.services.testcontainers.ContextContainer
import com.nortal.test.services.testcontainers.images.builder.ReusableImageFromDockerfile
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KGenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import java.io.File
import java.time.Duration

@Component
@ConditionalOnProperty(value = ["test-automation.containers.postgre.enabled"], havingValue = "true")
class AmsPostgreContainer : ContextContainer, JdbcUrlProvider {
    val container: GenericContainer<*> = configure()

    companion object {
        private const val NETWORK_ALIAS = "postgres"
        private const val POSTGRE_INTERNAL_PORT = 5432
    }

    override fun start(network: Network?) {
        container.withNetwork(network)
            .start()
    }

    private fun verifyRunning() {
        if (!container.isRunning) {
            throw AssertionError(
                "AMS postgre container is not started yet! Please start the context before trying to call this method."
            )
        }
    }

    private fun configure(): GenericContainer<*> {
        val imageFromDockerfile = ReusableImageFromDockerfile()
            .withDockerfile(File("../database/docker/Dockerfile").toPath())

        return KGenericContainer(imageFromDockerfile)
            .withNetworkAliases(NETWORK_ALIAS)
            .withExposedPorts(POSTGRE_INTERNAL_PORT)
            .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
            .withEnv("POSTGRES_PASSWORD", "postgres")
            .withEnv("POSTGRES_USER", "postgres")
            .withStartupTimeout(Duration.ofMinutes(1))
    }

    override fun getInternalJdbcUrl(): String {
        verifyRunning()
        return ("jdbc:postgresql://" + NETWORK_ALIAS + ":" + PostgreSQLContainer.POSTGRESQL_PORT
                + "/project_api_db")
    }

    override fun getJdbcUrl(): String {
        verifyRunning()
        return ("jdbc:postgresql://" + container.host + ":" + container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)
                + "/project_api_db")
    }
}
