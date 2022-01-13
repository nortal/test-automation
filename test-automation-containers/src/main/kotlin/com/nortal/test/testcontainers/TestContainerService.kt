package com.nortal.test.testcontainers

import com.github.dockerjava.api.model.Container
import com.nortal.test.core.services.TestableApplicationInfoProvider
import com.nortal.test.testcontainers.configuration.TestableContainerProperties
import com.nortal.test.testcontainers.images.builder.ImageFromDockerfile
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.CustomFixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.MountableFile
import java.time.Duration
import java.util.*

/**
 * This service is responsible for initializing and maintaining the black box testing setup.
 */
@Service
open class TestContainerService(
    private val testContainerNetworkProvider: TestContainerNetworkProvider,
    private val testableContainerProperties: TestableContainerProperties
) : TestableApplicationInfoProvider {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private var exposedContainerHost: String? = null
    private var exposedContainerPort: Int? = null

    @JvmOverloads
    fun startApplicationUnderTest(
        image: ImageFromDockerfile, fixedExposedPort: IntArray = IntArray(0), envConfig: Map<String, String> = mapOf()
    ) {
        val customFixedHostPortGenericContainer = CustomFixedHostPortGenericContainer(image)
        val allExposedPorts: MutableList<Int> = ArrayList()
        allExposedPorts.add(testableContainerProperties.internalHttpPort)
        for (fixedPort in fixedExposedPort) {
            customFixedHostPortGenericContainer.withFixedExposedPort(fixedPort, fixedPort)
            allExposedPorts.add(fixedPort)
        }
        val applicationContainer =
            customFixedHostPortGenericContainer.withNetwork(testContainerNetworkProvider.network)
                .withExposedPorts(*allExposedPorts.toTypedArray())
                .withEnv(envConfig)
                .withStartupTimeout(Duration.ofSeconds(testableContainerProperties.startupTimeout))

        if (testableContainerProperties.jacoco.enabled) {
            val mountableFile = MountableFile.forClasspathResource("jacoco/org.jacoco.agent.jar")
            applicationContainer
                .withCommand(getJacocoPort())
                .withCopyFileToContainer(mountableFile, "/jacocoagent.jar")
        }

        stopContainersOfOlderImage(image)
        startContainer(applicationContainer)
    }

    private fun getJacocoPort(): String {
        return String.format("-javaagent:/jacocoagent.jar=address=*,port=%d,output=tcpserver", testableContainerProperties.jacoco.port)
    }

    private fun stopContainersOfOlderImage(image: ImageFromDockerfile) {
        val imageNameWithVersion = image.dockerImageName
        if (imageNameWithVersion.contains(":")) {
            val split: Array<String> = imageNameWithVersion.split(":").toTypedArray()
            val imageName = split[0]
            val version = split[1]
            val containersRunning = DockerClientFactory.instance().client().listContainersCmd().exec()

            containersRunning.stream()
                .filter { container: Container -> container.image.contains(imageName) }
                .filter { container: Container -> !container.image.contains(version) }
                .forEach { containerToKill: Container ->
                    run {
                        log.info("Killing container {}", containerToKill.image)
                        DockerClientFactory.instance().client().stopContainerCmd(containerToKill.id).exec()
                    }
                }
        }
    }

    protected open fun startContainer(applicationContainer: GenericContainer<*>) {
        log.info("Starting application container")
        val timer = StopWatch.createStarted()
        if (testableContainerProperties.reuseBetweenRuns) {
            ContainerUtils.overrideNetworkAliases(applicationContainer, Collections.emptyList())
            applicationContainer.withReuse(true).start()
        } else {
            applicationContainer.start()
        }
        log.info("Application container started in {}ms", timer.time)
        exposedContainerPort = applicationContainer.getMappedPort(testableContainerProperties.internalHttpPort)
        exposedContainerHost = applicationContainer.host
        log.info(
            "Mapping the exposed internal application on {} port of {} to {}", exposedContainerHost,
            testableContainerProperties.internalHttpPort, exposedContainerPort
        )
    }

    override fun getHost(): String {
        return exposedContainerHost ?: throw AssertionError("Testable container was not initialized. Check execution order.")
    }

    override fun getPort(): Int {
        return exposedContainerPort ?: throw AssertionError("Testable container was not initialized. Check execution order.")
    }

}