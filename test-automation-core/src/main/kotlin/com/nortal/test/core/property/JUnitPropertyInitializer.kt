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
import com.nortal.test.core.configuration.TestAutomationConstants.FRAMEWORK_BOOTSTRAP_GLUE
import com.nortal.test.core.configuration.TestAutomationConstants.PARALLEL_CONFIG_STRATEGY_PROPERTY
import com.nortal.test.core.configuration.TestAutomationConstants.PROPERTY_BOOTSTRAP_GLUE_APPEND
import com.nortal.test.core.configuration.TestAutomationConstants.PROPERTY_PARALLEL_EXECUTION_GROUP_TAGS
import com.nortal.test.core.configuration.TestAutomationConstants.PROPERTY_PARALLEL_EXECUTOR_COUNT
import com.nortal.test.core.configuration.TestAutomationConstants.PROPERTY_PARALLEL_ISOLATION_TAG
import com.nortal.test.core.cucumber.SystemPropertiesProvider
import com.nortal.test.core.exception.TestAutomationException
import io.cucumber.core.options.Constants.EXECUTION_ORDER_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.*
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object JUnitPropertyInitializer {
    private const val PROPERTY_PREFIX = "test-automation."

    private val OPTIONAL_CUCUMBER_PROPERTIES = listOf(
        PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME,
        EXECUTION_DRY_RUN_PROPERTY_NAME,
        EXECUTION_ORDER_PROPERTY_NAME,
        FILTER_NAME_PROPERTY_NAME,
        FILTER_TAGS_PROPERTY_NAME
    )

    private val log = LoggerFactory.getLogger(JUnitPropertyInitializer.javaClass)
    private val propertyLoader = PropertyLoader()
    private var initialized = AtomicBoolean(false)


    @JvmStatic
    fun initializeProperties(): JUnitPropertyInitializer {
        if (!initialized.get()) {
            synchronized(initialized) {

                val env = propertyLoader.loadProperties()

                applyGenericProperties(env)
                applyHardCodedProperties()
                applySystemProperties(env)
                applyProviderSystemProperties(env)
                applyGlueAppendProperty(env)
                applyParallelExecutorConfig(env)

                initialized.set(true)
            }
        }
        return this
    }

    private fun applyGenericProperties(env: Environment) {
        env.getProperty("test-automation.report.allure.base-dir")?.let { System.setProperty("testExecLogDir", it) }
    }

    private fun applyHardCodedProperties() {
        System.setProperty(PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, false.toString())
        System.setProperty(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, true.toString())
        System.setProperty(GLUE_PROPERTY_NAME, FRAMEWORK_BOOTSTRAP_GLUE)
    }


    private fun applyParallelExecutorConfig(env: Environment) {
        if (env.getProperty(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, false.toString()).toBoolean()) {
            System.setProperty(PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME, PARALLEL_CONFIG_STRATEGY_PROPERTY)

            val propertyKey = PROPERTY_PARALLEL_EXECUTOR_COUNT
            val property = env.getProperty(propertyKey)

            if (property != null) {
                applyExecutionGroupConfig(env)
                System.setProperty(PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME, property)
            } else {
                throw TestAutomationException("Missing required property [$propertyKey] in application-<$ALL_SPRING_PROFILES>.yml")
            }
        }
    }

    private fun applyExecutionGroupConfig(env: Environment) {
        env.getProperty(PROPERTY_PARALLEL_ISOLATION_TAG)?.let {
            val propertyKey = getExecutionGroupPropertyKey(it)
            System.setProperty(
                propertyKey,
                "org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_KEY"
            )
        }

        env.getProperty(PROPERTY_PARALLEL_EXECUTION_GROUP_TAGS)?.let {
            it.split(",").forEach { tag ->
                val propertyKey = getExecutionGroupPropertyKey(tag)

                val sanitizedTag = sanitizeTagName(tag)
                System.setProperty(propertyKey, sanitizedTag)
            }
        }
    }

    private fun getExecutionGroupPropertyKey(tag: String): String {
        return EXECUTION_EXCLUSIVE_RESOURCES_READ_WRITE_TEMPLATE.replace(
            EXECUTION_EXCLUSIVE_RESOURCES_TAG_TEMPLATE_VARIABLE,
            sanitizeTagName(tag)
        )
    }

    private fun sanitizeTagName(tag: String): String {
        return tag.replaceFirst("@", "")
    }

    private fun applySystemProperties(env: Environment) {
        OPTIONAL_CUCUMBER_PROPERTIES.forEach { cucumberProperty ->
            val propertyKey = PROPERTY_PREFIX + cucumberProperty
            val property = env.getProperty(propertyKey)

            property?.let {
                val value = if (System.getProperty(propertyKey) == null) it else System.getProperty(propertyKey)

                System.setProperty(cucumberProperty, value)
                log.info("Cucumber prop [{}] was set to [{}]", cucumberProperty, value)
            }
        }
    }

    private fun applyGlueAppendProperty(env: Environment) {
        val propertyKey = PROPERTY_BOOTSTRAP_GLUE_APPEND
        val property = env.getProperty(propertyKey)

        property?.let {
            if (StringUtils.isNotBlank(it))
                System.setProperty(GLUE_PROPERTY_NAME, "$FRAMEWORK_BOOTSTRAP_GLUE,$it")
        }

    }

    private fun applyProviderSystemProperties(env: Environment) {
        val loader = ServiceLoader.load(SystemPropertiesProvider::class.java)

        loader.forEach { propertiesProvider ->
            val properties: Map<String, String> = propertiesProvider.getProperties()
            properties.forEach { providerProperty ->
                val propertyKey = PROPERTY_PREFIX + providerProperty.key
                val property = env.getProperty(propertyKey)

                property?.let { System.setProperty(providerProperty.value, it) }
            }
        }

    }

}