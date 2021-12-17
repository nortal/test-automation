package com.nortal.test.testcontainers

import com.nortal.test.testcontainers.configuration.ContainerProperties
import com.nortal.test.testcontainers.configuration.TestableContainerProperties
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

        containerProperties.contextContainers[getConfigurationKey()]?.reuseBetweenRuns?.let { newTestContainer.withReuse(it) }

        log.info("Starting container [{}] reuse-between-runs: {}", javaClass.name, newTestContainer.isShouldBeReused)
        newTestContainer.start()

        testContainer = newTestContainer
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