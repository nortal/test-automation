package com.nortal.test.testcontainers

import com.github.dockerjava.api.model.Container
import com.nortal.test.core.services.TestableApplicationPortProvider
import com.nortal.test.testcontainers.configuration.ContainerProperties
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.CustomFixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.KGenericContainer
import com.nortal.test.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.MountableFile
import java.time.Duration
import java.util.*

/**
 * This service is responsible for initializing and maintaining the black box testing setup.
 */
@Service
open class TestContainerService(
    private val testContainerNetworkProvider: TestContainerNetworkProvider,
    private val containerProperties: ContainerProperties,
) : TestableApplicationPortProvider {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private var exposedContainerPort: Int? = null

    companion object {
        private val TIMEOUT = Duration.ofSeconds(120)
        private const val INTERNAL_HTTP_PORT = 8080
    }


    /**
     * Starts the provided application image in a container.
     * Note: server.port must be set to 8080 for the correct port to be exposed.
     *
     * @param image   of the application under test
     * @param timeout of the application under test
     */
    @JvmOverloads
    fun startApplicationUnderTest(image: ImageFromDockerfile, timeout: Duration = TIMEOUT) {
        val applicationContainer: GenericContainer<*> =
            KGenericContainer(image).withNetwork(testContainerNetworkProvider.network).withExposedPorts(INTERNAL_HTTP_PORT)
                .withStartupTimeout(timeout)
        stopContainersOfOlderImage(image)
        startContainer(applicationContainer)
    }

    fun startApplicationUnderTest(
        image: ImageFromDockerfile, fixedExposedPort: IntArray, envConfig: Map<String, String>
    ) {
        val customFixedHostPortGenericContainer = CustomFixedHostPortGenericContainer(image)
        val allExposedPorts: MutableList<Int?> = ArrayList()
        allExposedPorts.add(INTERNAL_HTTP_PORT)
        for (fixedPort in fixedExposedPort) {
            customFixedHostPortGenericContainer.withFixedExposedPort(fixedPort, fixedPort)
            allExposedPorts.add(fixedPort)
        }
        val applicationContainer =
            customFixedHostPortGenericContainer.withNetwork(testContainerNetworkProvider.network)
                .withExposedPorts(*allExposedPorts.toTypedArray())
                .withEnv(envConfig)
                .withCopyFileToContainer(MountableFile.forClasspathResource("jacoco/org.jacoco.agent.jar"), "/jacocoagent.jar")
                .withCommand(getJacocoPort()).withStartupTimeout(TIMEOUT)

        stopContainersOfOlderImage(image)
        startContainer(applicationContainer)
    }

    private fun getJacocoPort(): String {
        return String.format("-javaagent:/jacocoagent.jar=address=*,port=%d,output=tcpserver", containerProperties.testableContainer.jacoco.port)
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
        if (containerProperties.testableContainer.reuseBetweenRuns) {
            ContainerUtils.overrideNetworkAliases(applicationContainer, Collections.emptyList())
            applicationContainer.withReuse(true).start()
        } else {
            applicationContainer.start()
        }
        log.info("Application container started in {}ms", timer.time)
        exposedContainerPort = applicationContainer.getMappedPort(INTERNAL_HTTP_PORT)
        log.info("Mapping the exposed internal application port of {} to {}", INTERNAL_HTTP_PORT, exposedContainerPort)
    }

    override fun getPort(): Int {
        return exposedContainerPort ?: throw AssertionError("Testable container was not initialized. Check execution order.")
    }

}