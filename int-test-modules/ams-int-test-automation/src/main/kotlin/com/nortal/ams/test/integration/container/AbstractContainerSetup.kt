package com.nortal.ams.test.integration.container

import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.services.testcontainers.TestContainerService
import com.nortal.test.services.testcontainers.images.builder.ReusableImageFromDockerfile
import java.io.File

abstract class AbstractContainerSetup(
    private val containerService: TestContainerService,
) : BeforeSuiteHook {

    companion object {
        const val DEFAULT_APP_DEBUG_PORT = 9000
    }

    /**
     * Environmental settings for the target container.
     */
    open fun getTargetContainerEnvConfig(): Map<String, String?> {
        return mapOf(
            "LABEL" to "cucumber-test",
            "spring.profiles.active" to "cucumber"
        )
    }

    /**
     * Defines ports that will be exposed to external access. Example: debug port.
     */
    open fun getTargetContainerExposedPorts(): IntArray {
        return intArrayOf(DEFAULT_APP_DEBUG_PORT)
    }

    override fun beforeSuite() {
        val imageFromDockerfile = ReusableImageFromDockerfile()
            .withDockerfile(File("../Dockerfile").toPath())

        containerService.startApplicationUnderTest(
            imageFromDockerfile,
            getTargetContainerExposedPorts(),
            getTargetContainerEnvConfig()
        )
    }

}
