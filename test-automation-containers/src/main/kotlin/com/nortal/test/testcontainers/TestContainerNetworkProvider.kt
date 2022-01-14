package com.nortal.test.testcontainers

import com.github.dockerjava.api.command.CreateNetworkCmd
import com.nortal.test.core.configuration.TestAutomationProperties
import com.nortal.test.testcontainers.configuration.ContainerProperties
import com.nortal.test.testcontainers.model.ReusedNetwork
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.Network
import java.util.*
import javax.annotation.PostConstruct

@Component
class TestContainerNetworkProvider(
    private val testAutomationProperties: TestAutomationProperties,
    private val containerProperties: ContainerProperties
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)
    var network: Network? = null

    companion object {
        private const val REUSABLE_NETWORK_NAME_PREFIX: String = "TEST-AUTOMATION-REUSABLE-NETWORK-"
    }

    /**
     * Runs containers of context applications in parallel, also sets up db schema and redis.
     */
    @PostConstruct
    fun startContext() {
        network = if (containerProperties.testableContainer.reuseBetweenRuns) {
            getReusableNetwork()
        } else {
            Network.newNetwork()
        }
        log.info("Initializing docker network.. ID: [{}]", network?.id)
    }

    private fun getReusableNetwork(): Network {
        val reusableNetworkOptional = findExistingReusableNetwork()
        return if (reusableNetworkOptional.isEmpty) {
            createReusableNetwork()
        } else {
            ReusedNetwork(reusableNetworkOptional.get().id)
        }

    }

    private fun getReusableNetworkName(): String {
        return REUSABLE_NETWORK_NAME_PREFIX + testAutomationProperties.environment
    }

    private fun createReusableNetwork(): Network {
        return Network.builder()
            .createNetworkCmdModifier { createNetworkCmd: CreateNetworkCmd -> createNetworkCmd.withName(getReusableNetworkName()) }
            .build()
    }

    private fun findExistingReusableNetwork(): Optional<com.github.dockerjava.api.model.Network> {
        val networks = DockerClientFactory.instance().client().listNetworksCmd().exec()
        return networks.stream().filter { ntwk: com.github.dockerjava.api.model.Network? -> ntwk!!.name == getReusableNetworkName() }.findAny()
    }
}