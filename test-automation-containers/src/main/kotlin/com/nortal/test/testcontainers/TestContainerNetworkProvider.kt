/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nortal.test.testcontainers

import com.github.dockerjava.api.command.CreateNetworkCmd
import com.nortal.test.core.configuration.TestAutomationProperties
import com.nortal.test.testcontainers.configuration.ContainerProperties
import com.nortal.test.testcontainers.model.ReusedNetwork
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.Network
import java.util.*

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
        return if (!reusableNetworkOptional.isPresent) {
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