/**
 * Copyright (c) 2023 Nortal AS
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
package com.nortal.test.testcontainers.configurator

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.LazyFuture

interface TestContainerConfigurator {

    fun imageDefinition(): LazyFuture<String>

    fun exposedPorts(): List<Int> = emptyList()

    fun fixedExposedPorts(): List<Int> = emptyList()

    fun environmentalVariables(): Map<String, String>

    interface TestContainerInitListener {
        fun beforeStart(container: GenericContainer<*>)

        fun afterStart(container: GenericContainer<*>)
    }

    interface TestContainerCustomizer {

        fun additionalEnvironmentalVariables(): Map<String, String> = emptyMap()

        fun additionalExposedPorts(): List<Int> = emptyList()

    }
}