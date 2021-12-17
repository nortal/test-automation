package com.nortal.ams.test.integration.container

import com.nortal.test.jdbc.JdbcUrlProvider
import com.nortal.test.testcontainers.ContextualContainer
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
class AmsPostgreContainer : ContextualContainer, JdbcUrlProvider {
    private var usingDefaultBridgeNetwork: Boolean = false

    val container: GenericContainer<*> = configure()

    companion object {
        private const val NETWORK_ALIAS = "postgres"
        private const val POSTGRE_INTERNAL_PORT = 5432
    }

    override fun start(network: Network?) {
        if (network != null) {
            container
                .withNetwork(network)
                .withNetworkAliases(NETWORK_ALIAS)
                .start()
        } else {
            usingDefaultBridgeNetwork = true
            container.start()
        }
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
            .withExposedPorts(POSTGRE_INTERNAL_PORT)
            .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
            .withEnv("POSTGRES_PASSWORD", "postgres")
            .withEnv("POSTGRES_USER", "postgres")
            .withStartupTimeout(Duration.ofMinutes(1))
    }

    override fun getInternalJdbcUrl(): String {
        verifyRunning()
        return String.format(
            "jdbc:postgresql://%s:%d/project_api_db",
            if (usingDefaultBridgeNetwork) {
                this.container.containerInfo.networkSettings.networks["bridge"]?.ipAddress
            } else {
                NETWORK_ALIAS
            },
            PostgreSQLContainer.POSTGRESQL_PORT
        )
    }

    override fun getJdbcUrl(): String {
        verifyRunning()
        return String.format(
            "jdbc:postgresql://%s:%d/project_api_db",
            container.host,
            container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)
        )
    }
}
