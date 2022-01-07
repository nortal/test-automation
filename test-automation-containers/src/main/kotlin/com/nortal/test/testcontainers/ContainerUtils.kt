package com.nortal.test.testcontainers

import org.apache.commons.lang3.reflect.FieldUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer

object ContainerUtils {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun overrideNetworkAliases(genericContainer: GenericContainer<*>?, aliases: List<String?>?) {
        try {
            FieldUtils.writeField(genericContainer, "networkAliases", aliases, true)
        } catch (e: IllegalAccessException) {
            log.error("Failed to override network alias", e)
        }
    }
}