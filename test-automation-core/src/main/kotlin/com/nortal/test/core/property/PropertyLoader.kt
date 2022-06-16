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