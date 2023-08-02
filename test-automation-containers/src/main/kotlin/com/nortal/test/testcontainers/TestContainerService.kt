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

import com.nortal.test.core.services.TestableApplicationInfoProvider
import com.nortal.test.testcontainers.configuration.TestableContainerProperties
import com.nortal.test.testcontainers.configurator.TestContainerConfigurator
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.testcontainers.containers.ContainerLaunchException
import org.testcontainers.containers.CustomFixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This service is responsible for initializing and maintaining the black box testing setup.
 */
@Service
open class TestContainerService(
    private val testContainerNetworkProvider: TestContainerNetworkProvider,
    private val testableContainerProperties: TestableContainerProperties,
    private val testContainerConfigurator: TestContainerConfigurator,
    private val initListeners: List<TestContainerConfigurator.TestContainerInitListener>
) : TestableApplicationInfoProvider, TestableApplicationContainerProvider, TestableContainerInitializer {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private var exposedContainerHost: String? = null
    private var exposedContainerPort: Int? = null

    private var runningContainer: GenericContainer<*>? = null

    companion object {
        val INIT_FAILED = AtomicBoolean(false)

        const val TESTCONTAINERS_IMAGE_LABEL = "test-automation.image"
        private const val ERROR_NOT_INITIALIZED = "Testable container was not initialized. Check execution order."
    }

    override fun initialize() {
        val logger = LoggerFactory.getLogger(testableContainerProperties.internalNetworkAlias)
        val logConsumer = Slf4jLogConsumer(logger).withSeparateOutputStreams()
        val imageDefinition = testContainerConfigurator.imageDefinition()
        val container =
            CustomFixedHostPortGenericContainer(imageDefinition)
                .withNetwork(testContainerNetworkProvider.network)
                .withExposedPorts(*testContainerConfigurator.exposedPorts().toTypedArray())
                .withEnv(testContainerConfigurator.environmentalVariables())
                .withLogConsumer(logConsumer)
                .withNetworkAliases(testableContainerProperties.internalNetworkAlias)
                .withReuse(testableContainerProperties.reuseBetweenRuns)
                .withLabel(TESTCONTAINERS_IMAGE_LABEL, imageDefinition.dockerImageNameWithVersion)
                .withStartupTimeout(Duration.ofSeconds(testableContainerProperties.startupTimeout))

        testContainerConfigurator.fixedExposedPorts().forEach {
            container.withFixedExposedPort(it, it)
        }
        initListeners.forEach { it.beforeStart(container) }

        startContainer(container)

        initListeners.forEach { it.afterStart(container) }

        runningContainer = container
    }

    protected open fun startContainer(applicationContainer: GenericContainer<*>) {
        if (INIT_FAILED.get()) {
            log.warn("Previous attempt to initialize container has failed. Disabling any subsequent tries.")

        } else {
            log.info("Starting application container..")
            val timer = StopWatch.createStarted()
            if (testableContainerProperties.reuseBetweenRuns) {
                ContainerUtils.overrideNetworkAliases(applicationContainer, Collections.emptyList())
            }

            try {
                applicationContainer.start()
            } catch (exception: ContainerLaunchException) {
                INIT_FAILED.set(true)
                throw exception
            }

            log.info("Application container started in {} ms", timer.time)
            val firstPort = testContainerConfigurator.exposedPorts().first()
            exposedContainerPort = applicationContainer.getMappedPort(firstPort)
            exposedContainerHost = applicationContainer.host

            log.info(
                "Mapping the exposed internal application on {} port of {} to {}", exposedContainerHost,
                firstPort, exposedContainerPort
            )
        }
    }

    override fun getHost(): String {
        return exposedContainerHost ?: throw AssertionError(ERROR_NOT_INITIALIZED)
    }

    override fun getPort(): Int {
        return exposedContainerPort ?: throw AssertionError(ERROR_NOT_INITIALIZED)
    }

    override fun getMappedPort(internalPort: Int): Int {
        return runningContainer?.getMappedPort(internalPort) ?: throw AssertionError(ERROR_NOT_INITIALIZED)
    }

    override fun getContainer(): GenericContainer<*> {
        return runningContainer ?: throw AssertionError(ERROR_NOT_INITIALIZED)
    }
}