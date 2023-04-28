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
package com.nortal.test.testcontainers

import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class TestContextContainerService(
    private val contextContainers: Collection<AuxiliaryContainer<*>>,
    private val testContainerNetworkProvider: TestContainerNetworkProvider,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)


    /**
     * Runs containers of context applications in parallel.
     */
    fun startContext() {

        log.info("Starting context for {} containers", contextContainers.size)
        val timer = StopWatch.createStarted()

        contextContainers.parallelStream().forEach { startContainer(it) }
        log.info("Context containers started in {} ms", timer.time)
    }

    protected open fun startContainer(auxiliaryContainer: AuxiliaryContainer<*>){
        val network = testContainerNetworkProvider.network

        auxiliaryContainer.start(network)
    }
}