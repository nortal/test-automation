package com.nortal.test.core.services

interface TestableApplicationInfoProvider {
    /**
     * Get Host from which container is accessible. May not be localhost.
     */
    fun getHost(): String

    /**
     * Get open port to access the container.
     */
    fun getPort(): Int
}