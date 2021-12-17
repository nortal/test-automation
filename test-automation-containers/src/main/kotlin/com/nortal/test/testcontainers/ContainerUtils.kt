package com.nortal.test.testcontainers

import org.apache.commons.lang3.reflect.FieldUtils
import org.testcontainers.containers.GenericContainer
import java.lang.IllegalAccessException

object ContainerUtils {
    fun overrideNetworkAliases(genericContainer: GenericContainer<*>?, aliases: List<String?>?) {
        try {
            FieldUtils.writeField(genericContainer, "networkAliases", aliases, true)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}