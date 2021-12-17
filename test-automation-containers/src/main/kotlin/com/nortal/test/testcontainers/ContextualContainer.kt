package com.nortal.test.testcontainers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network

/**
 * TODO: rename
 * Interface marking the container as a context one.
 * Context containers are initialized and started before the system under test.
 */
interface ContextualContainer<T : GenericContainer<T>> {

    fun start(network: Network?)

    /**
     * Configuration key which is used in configuration.
     */
    fun getConfigurationKey(): String

    fun getTestContainer(): T
}