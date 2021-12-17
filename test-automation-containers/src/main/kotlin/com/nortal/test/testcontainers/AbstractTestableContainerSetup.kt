package com.nortal.test.testcontainers

import com.nortal.test.core.services.hooks.BeforeSuiteHook
import com.nortal.test.core.services.hooks.BeforeSuiteHook.Companion.DEFAULT_ORDER
import com.nortal.test.testcontainers.configuration.TestableContainerProperties
import com.nortal.test.testcontainers.images.builder.ImageFromDockerfile
import com.nortal.test.testcontainers.images.builder.ReusableImageFromDockerfile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * TODO:
 */
@Component
abstract class AbstractTestableContainerSetup : BeforeSuiteHook {
    @Autowired
    protected lateinit var testableContainerProperties: TestableContainerProperties

    @Autowired
    protected lateinit var containerService: TestContainerService

    companion object {
        private const val APP_JAR_PATH = "/app.jar"
        private const val JACOCO_CLASSPATH_PATH = "jacoco/org.jacoco.agent.jar"
        private const val JACOCO_AGENT_JAR_PATH = "/jacocoagent.jar"
    }

    override fun beforeSuiteOrder(): Int {
        return DEFAULT_ORDER - 100
    }

    override fun beforeSuite() {
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
        return intArrayOf(
            testableContainerProperties.debugPort,
            testableContainerProperties.jacoco.port
        )
    }

    abstract fun onContainerStartupInitiated()

    private fun build(): ImageFromDockerfile {
        val appJarDir = Paths.get(testableContainerProperties.jarBuildDir)
        val appJarPath = Files.find(appJarDir, 1, { t, _ -> isMatchingJarFile(t) })
            .findFirst().orElseThrow()


        val reusableImageFromDockerfile =
            ReusableImageFromDockerfile(createImageName(appJarPath), false, testableContainerProperties.reuseBetweenRuns)
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
        return path.fileName.toString().matches(Regex("^.+-.+.*(?<!plain)\\.jar\$"));
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
        commandParts.addAll(getDebugPart())
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
