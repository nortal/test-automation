package com.nortal.test.container

import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.services.testcontainers.TestContainerService
import com.nortal.test.services.testcontainers.images.builder.ReusableImageFromDockerfile
import java.io.File

abstract class AbstractContainerSetup(
    private val containerService: TestContainerService,
) : BeforeSuiteHook {

  open fun getTargetContainerEnvConfig(): Map<String, String?> {
    return mapOf(
        "LABEL" to "test",
        "spring.profiles.active" to "cucumber"
    )
  }

  open fun getTargetContainerExposedPorts(): IntArray {
    return intArrayOf()
  }

  override fun beforeSuite() {
    containerService.startContext()

    val imageFromDockerfile = ReusableImageFromDockerfile()
        .withDockerfile(File("../Dockerfile").toPath())

    containerService.startApplicationUnderTest(imageFromDockerfile,
        getTargetContainerExposedPorts(),
        getTargetContainerEnvConfig())
  }

}
