package com.nortal.test.testcontainers

import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
open class TestContextContainerService(
    private val contextContainers: Collection<ContextualContainer<*>>,
    private val testContainerNetworkProvider: TestContainerNetworkProvider,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)


    /**
     * Runs containers of context applications in parallel, also sets up db schema and redis.
     */
    @PostConstruct
    fun startContext() {

        log.info("Starting context for {} containers", contextContainers.size)
        val timer = StopWatch.createStarted()

        contextContainers.parallelStream().forEach { startContainer(it) }
        log.info("Context containers started in {}ms", timer.time)
    }

    protected open fun startContainer(contextualContainer: ContextualContainer<*>){
        val network = testContainerNetworkProvider.network

        contextualContainer.start(network)
    }
}