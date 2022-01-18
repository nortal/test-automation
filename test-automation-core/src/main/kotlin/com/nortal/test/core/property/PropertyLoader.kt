package com.nortal.test.core.property

import com.nortal.test.core.configuration.TestAutomationConstants.ALL_SPRING_PROFILES
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.Environment
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.ClassPathResource

/**
 * A simplified version of spring boot property loader. Its purpose is to have an access to properties before spring boot loads.
 */
class PropertyLoader {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val yamlPropertySourceLoader = YamlPropertySourceLoader()
    private val propertyFileTemplate = "application.yml"

    private val yamlExtensions = arrayListOf("yml", "yaml")
    private val propertyFileWithProfileTemplate = "application-%s.%s"

    fun loadProperties(): Environment {
        val env = StandardEnvironment()

        val configurationFiles = mutableListOf(propertyFileTemplate)
        ALL_SPRING_PROFILES.forEach {
            configurationFiles.add(String.format(propertyFileWithProfileTemplate, it, yamlExtensions[0]))
            configurationFiles.add(String.format(propertyFileWithProfileTemplate, it, yamlExtensions[1]))
        }

        configurationFiles.forEach {
            val resource = ClassPathResource(it)
            if (resource.isReadable) {
                yamlPropertySourceLoader.load(it, resource).forEach { source ->
                    env.propertySources.addFirst(source)
                }
                log.info("Initialized property config: {}", resource.path)
            }
        }


        return env
    }
}