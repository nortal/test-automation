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
package com.nortal.test.testcontainers.images.builder

import com.github.dockerjava.api.command.BuildImageCmd
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.DockerClientFactory
import org.testcontainers.images.DockerTestUtils

class ReusableImageFromDockerfile(
    dockerImageName: String,
    deleteOnExit: Boolean,
    private val reusableContainer: Boolean,
) : ImageFromDockerfile(dockerImageName, deleteOnExit) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun addApplicableLabels(buildImageCmd: BuildImageCmd?) {
        val labels: MutableMap<String, String> = HashMap()
        if (buildImageCmd!!.labels != null) {
            labels.putAll(buildImageCmd.labels!!)
        }

        if (reusableContainer) {
            labels.putAll(removeSessionIdLabel(DockerClientFactory.DEFAULT_LABELS))
        } else
            labels.putAll(DockerClientFactory.DEFAULT_LABELS)

        buildImageCmd.withLabels(labels)
    }

    override fun get(): String {
        if (reusableContainer) {
            if (DockerTestUtils.isContainerOfImageRunning(dockerImageNameWithVersion)) {
                log.warn("Skipping DockerFile build as reusable container is enabled and container is running!")
                return dockerImageNameWithVersion
            }
        }
        return super.get()
    }

    private fun removeSessionIdLabel(labels: Map<String, String>): Map<String, String> {
        val defaultLabels: MutableMap<String, String> = java.util.HashMap(labels)
        defaultLabels.remove(DockerClientFactory.TESTCONTAINERS_SESSION_ID_LABEL)
        return defaultLabels
    }
}