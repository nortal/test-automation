package com.nortal.test.testcontainers.images.builder

import com.github.dockerjava.api.command.BuildImageCmd
import org.testcontainers.DockerClientFactory

class ReusableImageFromDockerfile(
    dockerImageName: String,
    deleteOnExit: Boolean,
    private val reusableContainer: Boolean,
) : ImageFromDockerfile(dockerImageName, deleteOnExit) {

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

    private fun removeSessionIdLabel(labels: Map<String, String>): Map<String, String> {
        val defaultLabels: MutableMap<String, String> = java.util.HashMap(labels)
        defaultLabels.remove(DockerClientFactory.TESTCONTAINERS_SESSION_ID_LABEL)
        return defaultLabels
    }
}