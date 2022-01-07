package com.nortal.test.core.util

import org.junit.jupiter.api.Assertions.assertTrue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object RetryingInvoker {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @JvmStatic
    @JvmOverloads
    fun <T> retry(
        message: String = "", atMostSeconds: Long = 60, pollIntervalSeconds: Long = 2,
        assertTrue: Boolean = false,
        assertFalse: Boolean = false,
        block: () -> T
    ): T {
        val start = System.currentTimeMillis()
        var throwable: Throwable? = null

        var i = 0
        while ((System.currentTimeMillis() - start) < (atMostSeconds * 1000)) {
            i++
            if (message != "")
                log.info(message)

            try {
                if (assertTrue) {
                    assertTrue(block() == true, "Validation failed")
                    @Suppress("UNCHECKED_CAST")
                    return null as T
                }
                if (assertFalse) {
                    assertTrue(block() == false, "Validation failed")
                    @Suppress("UNCHECKED_CAST")
                    return null as T
                }
                return block()
            } catch (e: Throwable) {
                throwable = e
                log.error(e.message)
                log.trace("Failed retry attempt $i. Source: ${block.javaClass}")
            }
            Thread.sleep((pollIntervalSeconds * 1000))
        }
        throw throwable!!
    }
}