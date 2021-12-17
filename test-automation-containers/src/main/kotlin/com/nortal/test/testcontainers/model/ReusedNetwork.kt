package com.nortal.test.testcontainers.model

import org.junit.rules.ExternalResource
import org.testcontainers.containers.Network
import org.testcontainers.utility.ResourceReaper

/**
 * Reused network with already predefined id.
 */
class ReusedNetwork(private val id: String) : ExternalResource(), Network {
    override fun getId(): String {
        return id
    }

    override fun close() {
        ResourceReaper.instance().removeNetworkById(id)
    }
}