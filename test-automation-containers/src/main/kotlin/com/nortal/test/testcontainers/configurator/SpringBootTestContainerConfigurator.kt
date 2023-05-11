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

import com.nortal.test.core.exception.TestConfigurationException
import com.nortal.test.testcontainers.configuration.SpringBootTestContainerProperties
import com.nortal.test.testcontainers.configuration.TestableContainerProperties
import com.nortal.test.testcontainers.images.builder.ImageFromDockerfile
import com.nortal.test.testcontainers.images.builder.ReusableImageFromDockerfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder
import org.testcontainers.utility.LazyFuture
import org.testcontainers.utility.MountableFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

open class SpringBootTestContainerConfigurator :
    TestContainerConfigurator,
    TestContainerConfigurator.TestContainerInitListener {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var testableContainerProperties: TestableContainerProperties

    @Autowired
    private lateinit var containerProperties: SpringBootTestContainerProperties

    @Autowired
    private lateinit var containerCustomizer: TestContainerCustomizer

    companion object {
        private const val APP_JAR_PATH = "/app.jar"
        private const val JACOCO_CLASSPATH_PATH = "jacoco/org.jacoco.agent.jar"
        private const val JACOCO_AGENT_JAR_PATH = "/jacocoagent.jar"
    }

    interface TestContainerCustomizer : TestContainerConfigurator.TestContainerCustomizer {
        /**
         * Customize Image definition.
         */
        fun customizeImageDefinition(reusableImageFromDockerfile: ImageFromDockerfile)

        /**
         * Additional commandParts which are added to dockerfile builder.
         */
        fun customizeCommandParts(): List<String> = emptyList()

        /**
         * Customize DockerfileBuilder.
         */
        fun customizeDockerFileBuilder(builder: DockerfileBuilder)
    }

    override fun imageDefinition(): LazyFuture<String> {
        var jarBuildDir = containerProperties.jarBuildDir
        val appJarDir = Paths.get(jarBuildDir)
        val appJarPath = Files.find(appJarDir, 1, { t, _ -> isMatchingJarFile(t) })
            .findFirst()
            .orElseThrow { TestConfigurationException("Failed to find jar in $jarBuildDir") }

        log.info("Will use {} jar for container creation", appJarPath.toString())

        val reusableImageFromDockerfile =
            ReusableImageFromDockerfile(
                testableContainerProperties.internalNetworkAlias,
                false,
                testableContainerProperties.reuseBetweenRuns
            )
                .withFileFromPath(APP_JAR_PATH, appJarPath)
                .withFileFromClasspath(
                    JACOCO_AGENT_JAR_PATH,
                    JACOCO_CLASSPATH_PATH
                )

        containerCustomizer.customizeImageDefinition(reusableImageFromDockerfile)

        reusableImageFromDockerfile.withDockerfileFromBuilder { builder -> configure(builder).build() }
        return reusableImageFromDockerfile
    }


    override fun beforeStart(container: GenericContainer<*>) {
        if (containerProperties.jacoco.enabled) {
            val mountableFile = MountableFile.forClasspathResource("jacoco/org.jacoco.agent.jar")
            container
                .withCopyFileToContainer(mountableFile, "/jacocoagent.jar")
        }
    }

    override fun afterStart(container: GenericContainer<*>) {
        //do nothing
    }

    override fun fixedExposedPorts(): List<Int> {
        val ports: MutableList<Int> = mutableListOf()

        if (containerProperties.jarDebugEnabled) {
            ports.add(containerProperties.debugPort)
        }

        return ports
    }

    override fun exposedPorts(): List<Int> {
        val ports: MutableList<Int> = mutableListOf()
        ports.addAll(containerCustomizer.additionalExposedPorts())

        if (containerProperties.jacoco.enabled) {
            ports.add(containerProperties.jacoco.port)
        }
        return ports
    }

    override fun environmentalVariables(): Map<String, String> {
        return mapOf(
            "spring.profiles.active" to containerProperties.springProfilesToActivate,
        ) + containerCustomizer.additionalEnvironmentalVariables()
    }

    private fun configure(builder: DockerfileBuilder): DockerfileBuilder {
        val baseImage = containerProperties.baseImage
        builder
            .from(baseImage)
            .copy(
                APP_JAR_PATH,
                APP_JAR_PATH
            )
            .entryPoint(*getCommandParts())

        if (containerProperties.jacoco.enabled) {
            builder.copy(
                JACOCO_AGENT_JAR_PATH,
                JACOCO_AGENT_JAR_PATH
            )
        }

        containerCustomizer.customizeDockerFileBuilder(builder)

        return builder
    }

    private fun isMatchingJarFile(path: Path): Boolean {
        return path.fileName.toString().matches(Regex(containerProperties.jarRegexMatcher))
    }

    private fun getCommandParts(): Array<String> {
        val commandParts: MutableList<String> = ArrayList()
        commandParts.add("java")
        commandParts.add(containerProperties.memorySettings)
        commandParts.addAll(containerCustomizer.customizeCommandParts())
        if (containerProperties.jarDebugEnabled) {
            commandParts.addAll(getDebugPart())
        }
        if (containerProperties.jacoco.enabled) {
            commandParts.add(getJacocoPart())
        }
        commandParts.add("-jar")
        commandParts.add(APP_JAR_PATH)
        return commandParts.toTypedArray()
    }

    private fun getJacocoPart(): String {
        return String.format(
            "-javaagent:/jacocoagent.jar=address=*,port=%d,output=tcpserver",
            containerProperties.jacoco.port
        )
    }

    private fun getDebugPart(): List<String> {
        return listOf(
            "-Xdebug",
            String.format(
                "-Xrunjdwp:transport=dt_socket,address=*:%d,server=y,suspend=%s",
                containerProperties.debugPort,
                if (containerProperties.waitForDebugger) "y" else "n"
            )
        )
    }


}