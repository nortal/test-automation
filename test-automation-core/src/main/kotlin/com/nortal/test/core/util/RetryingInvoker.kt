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