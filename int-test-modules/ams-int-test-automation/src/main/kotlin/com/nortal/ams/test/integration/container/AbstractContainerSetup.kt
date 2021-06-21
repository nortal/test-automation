package com.nortal.ams.test.integration.container

import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.services.testcontainers.TestContainerService
import com.nortal.test.testcontainers.configuration.TestableContainerProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * TODO: move to containers module.
 */
@Component
abstract class AbstractContainerSetup : BeforeSuiteHook {
    @Autowired
    private lateinit var testableContainerProperties: TestableContainerProperties

    @Autowired
    private lateinit var containerService: TestContainerService

    companion object {
        private const val APP_JAR_PATH = "/app.jar"
        private const val JACOCO_CLASSPATH_PATH = "jacoco/org.jacoco.agent.jar"
        private const val JACOCO_AGENT_JAR_PATH = "/jacocoagent.jar"
    }

    /**
     * Environmental settings for the target container.
     */
    fun getTargetContainerEnvConfig(): Map<String, String?> {
        return mapOf(
            "LABEL" to "cucumber-test",
            "spring.profiles.active" to testableContainerProperties.springProfilesToActivate
        )
    }

    /**
     * Defines ports that will be exposed to external access. Example: debug port.
     */
    fun getTargetContainerExposedPorts(): IntArray {
        return intArrayOf(
            testableContainerProperties.debugPort,
            testableContainerProperties.jacoco.port
        )
    }

    override fun beforeSuite() {
        containerService.startApplicationUnderTest(
            build(),
            getTargetContainerExposedPorts(),
            getTargetContainerEnvConfig()
        )
    }

    fun build(): ImageFromDockerfile {
        val appJarDir = Paths.get(testableContainerProperties.jarBuildDir)
        val appJarPath = Files.find(appJarDir, 1, { t, _ -> isMatchingJarFile(t) })
            .findFirst().orElseThrow()

        return ImageFromDockerfile()
            .withFileFromPath(APP_JAR_PATH, appJarPath)
            .withFileFromClasspath(JACOCO_AGENT_JAR_PATH, JACOCO_CLASSPATH_PATH)
            .withDockerfileFromBuilder { builder -> configure(builder).build() }
    }

    private fun isMatchingJarFile(path: Path): Boolean {
        return path.fileName.toString().matches(Regex("^.+-.+.*(?<!plain)\\.jar\$"));
    }

    private fun configure(builder: DockerfileBuilder): DockerfileBuilder {
        val baseImage = testableContainerProperties.baseImage
        return builder
            .from(baseImage)
            .copy(
                APP_JAR_PATH,
                APP_JAR_PATH
            )
            .copy(
                JACOCO_AGENT_JAR_PATH,
                JACOCO_AGENT_JAR_PATH
            )
            .entryPoint(*getCommandParts())
    }

    private fun getCommandParts(): Array<String> {
        val commandParts: MutableList<String> = ArrayList()
        commandParts.add("java")
        commandParts.add("-jar")
        commandParts.addAll(getDebugPart())
        if (testableContainerProperties.jacoco.enabled) {
            commandParts.add(getJacocoPart())
        }
        commandParts.add("/app.jar")
        return commandParts.toTypedArray()
    }


    private fun getJacocoPart(): String {
        return String.format(
            "-javaagent:/jacocoagent.jar=address=*,port=%d,output=tcpserver",
            testableContainerProperties.jacoco.port
        )
    }

    private fun getDebugPart(): List<String> {
        return listOf(
            "-Xdebug",
            String.format(
                "-Xrunjdwp:transport=dt_socket,address=*:%d,server=y,suspend=%s",
                testableContainerProperties.debugPort,
                if (testableContainerProperties.waitForDebugger) "y" else "n"
            )
        )
    }
}
