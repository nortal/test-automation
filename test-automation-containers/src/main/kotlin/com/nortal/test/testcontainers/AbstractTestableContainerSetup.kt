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

import com.nortal.test.core.exception.TestConfigurationException
import com.nortal.test.testcontainers.configuration.TestableContainerProperties
import com.nortal.test.testcontainers.images.builder.ImageFromDockerfile
import com.nortal.test.testcontainers.images.builder.ReusableImageFromDockerfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Base class for testable (under test) container setup.
 */
@Component
abstract class AbstractTestableContainerSetup : TestableContainerInitializer {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    protected lateinit var testableContainerProperties: TestableContainerProperties

    @Autowired
    protected lateinit var containerService: TestContainerService

    companion object {
        private const val APP_JAR_PATH = "/app.jar"
        private const val JACOCO_CLASSPATH_PATH = "jacoco/org.jacoco.agent.jar"
        private const val JACOCO_AGENT_JAR_PATH = "/jacocoagent.jar"
    }


    override fun initialize() {
        containerService.startApplicationUnderTest(
            build(),
            getTargetContainerExposedPorts(),
            getTargetContainerEnvConfig()
        )

        onContainerStartupInitiated()
    }

    /**
     * Environmental settings for the target container.
     */
    open fun getTargetContainerEnvConfig(): Map<String, String> {
        return mapOf()
    }

    /**
     * Defines ports that will be exposed to external access. Example: debug port.
     */
    open fun getTargetContainerExposedPorts(): IntArray {
        val ports: MutableList<Int> = mutableListOf()
        if (testableContainerProperties.jacoco.enabled) {
            ports.add(testableContainerProperties.jacoco.port)
        }
        if (testableContainerProperties.jarDebugEnabled) {
            ports.add(testableContainerProperties.debugPort)
        }
        return ports.toIntArray()
    }

    abstract fun onContainerStartupInitiated()

    protected open fun build(): ImageFromDockerfile {
        val appJarDir = Paths.get(testableContainerProperties.jarBuildDir)
        val appJarPath = Files.find(appJarDir, 1, { t, _ -> isMatchingJarFile(t) })
            .findFirst()
            .orElseThrow { TestConfigurationException("Failed to find jar in $testableContainerProperties.jarBuildDir") }

        log.info("Will use {} jar for container creation", appJarPath.toString())

        val reusableImageFromDockerfile =
            ReusableImageFromDockerfile(
                createImageName(appJarPath),
                false,
                testableContainerProperties.reuseBetweenRuns
            )
                .withFileFromPath(APP_JAR_PATH, appJarPath)
                .withFileFromClasspath(JACOCO_AGENT_JAR_PATH, JACOCO_CLASSPATH_PATH)

        additionalImageFromDockerfileConfiguration(reusableImageFromDockerfile)
        reusableImageFromDockerfile.withDockerfileFromBuilder { builder -> configure(builder).build() }
        return reusableImageFromDockerfile
    }

    private fun createImageName(appJarPath: Path): String {
        val jarFile = appJarPath.toFile()

        return applicationName() + ":" + jarFile.lastModified()
    }

    private fun isMatchingJarFile(path: Path): Boolean {
        return path.fileName.toString().matches(Regex(testableContainerProperties.jarRegexMatcher))
    }

    private fun configure(builder: DockerfileBuilder): DockerfileBuilder {
        val baseImage = testableContainerProperties.baseImage
        builder
            .from(baseImage)
            .copy(
                APP_JAR_PATH,
                APP_JAR_PATH
            )
            .entryPoint(*getCommandParts())

        if (testableContainerProperties.jacoco.enabled) {
            builder.copy(
                JACOCO_AGENT_JAR_PATH,
                JACOCO_AGENT_JAR_PATH
            )
        }
        additionalBuilderConfiguration(builder)
        return builder
    }

    abstract fun applicationName(): String

    abstract fun additionalBuilderConfiguration(builder: DockerfileBuilder)

    abstract fun additionalImageFromDockerfileConfiguration(reusableImageFromDockerfile: ImageFromDockerfile)

    /**
     * Additional commandParts which are added to dockerfile builder.
     */
    abstract fun additionalCommandParts(): List<String>

    /**
     * jvm -Xmx value. Value examples: 1G, 256M.
     */
    abstract fun maxMemory(): String

    private fun getCommandParts(): Array<String> {
        val commandParts: MutableList<String> = ArrayList()
        commandParts.add("java")
        commandParts.add("-jar")
        commandParts.add(getMaxMemoryPart())
        commandParts.addAll(additionalCommandParts())
        if (testableContainerProperties.jarDebugEnabled) {
            commandParts.addAll(getDebugPart())
        }
        if (testableContainerProperties.jacoco.enabled) {
            commandParts.add(getJacocoPart())
        }
        commandParts.add(APP_JAR_PATH)
        return commandParts.toTypedArray()
    }

    private fun getMaxMemoryPart(): String {
        return "-Xmx" + maxMemory()
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
