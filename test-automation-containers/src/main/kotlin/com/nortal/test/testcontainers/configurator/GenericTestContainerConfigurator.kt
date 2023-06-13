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

import com.nortal.test.testcontainers.configuration.TestableContainerProperties
import com.nortal.test.testcontainers.images.builder.ImageFromDockerfile
import com.nortal.test.testcontainers.images.builder.ReusableImageFromDockerfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

open class GenericTestContainerConfigurator(private val dockerImage: String) : TestContainerConfigurator {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var testableContainerProperties: TestableContainerProperties

    @Autowired
    private lateinit var containerCustomizer: TestContainerConfigurator.TestContainerCustomizer

    override fun imageDefinition(): ImageFromDockerfile {
        log.info("Will run tests against container with image $dockerImage")

        return ReusableImageFromDockerfile(
            dockerImage,
            false,
            testableContainerProperties.reuseBetweenRuns
        ).withDockerfileFromBuilder { it.from(dockerImage) }
    }

    override fun environmentalVariables(): Map<String, String> {
        return containerCustomizer.additionalEnvironmentalVariables()
    }

    override fun exposedPorts(): List<Int> {
        return containerCustomizer.additionalExposedPorts()
    }

}