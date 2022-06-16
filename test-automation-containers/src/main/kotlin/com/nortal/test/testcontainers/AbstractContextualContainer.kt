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

import com.nortal.test.testcontainers.configuration.ContainerProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network

abstract class AbstractContextualContainer<T : GenericContainer<T>> : ContextualContainer<T> {
    @Autowired
    private lateinit var containerProperties: ContainerProperties

    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private var testContainer: T? = null

    override fun start(network: Network?) {
        val newTestContainer = configure()
        if (network != null) {
            newTestContainer.withNetwork(network)
        }

        newTestContainer.withReuse(isReusable())

        log.info("Starting container [{}] reuse-between-runs: {}", javaClass.name, newTestContainer.isShouldBeReused)
        newTestContainer.start()

        testContainer = newTestContainer
    }

    private fun isReusable(): Boolean {
        return containerProperties.testableContainer.reuseBetweenRuns
                && containerProperties.contextContainers[getConfigurationKey()]?.reuseBetweenRuns == true
    }

    override fun getTestContainer(): T {
        verifyRunning()
        return testContainer!!
    }

    /**
     * Verify that container is running.
     * @throws AssertionError if container is not running
     */
    open fun verifyRunning() {
        if (!isRunning()) {
            throw AssertionError(
                "Contextual container is not started yet! Please start the context before trying to call this method."
            )
        }
    }

    open fun isRunning(): Boolean {
        return testContainer?.isRunning == true
    }

    abstract fun configure(): T
}