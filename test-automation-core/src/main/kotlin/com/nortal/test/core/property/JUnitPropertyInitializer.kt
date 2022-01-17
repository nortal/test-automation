package com.nortal.test.core.property

import com.nortal.test.core.configuration.TestAutomationConstants.ALL_SPRING_PROFILES
import com.nortal.test.core.configuration.TestAutomationConstants.FRAMEWORK_BOOTSTRAP_GLUE
import com.nortal.test.core.configuration.TestAutomationConstants.PARALLEL_CONFIG_STRATEGY_PROPERTY
import com.nortal.test.core.configuration.TestAutomationConstants.PROPERTY_BOOTSTRAP_GLUE_APPEND
import com.nortal.test.core.configuration.TestAutomationConstants.PROPERTY_PARALLEL_EXECUTION_GROUP_TAGS
import com.nortal.test.core.configuration.TestAutomationConstants.PROPERTY_PARALLEL_EXECUTOR_COUNT
import com.nortal.test.core.configuration.TestAutomationConstants.PROPERTY_PARALLEL_ISOLATION_TAG
import com.nortal.test.core.exception.TestAutomationException
import io.cucumber.core.options.Constants.EXECUTION_ORDER_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.*
import org.apache.commons.lang3.StringUtils
import org.springframework.core.env.Environment
import java.util.concurrent.atomic.AtomicBoolean

object JUnitPropertyInitializer {
    private const val PROPERTY_PREFIX = "test-automation."

    private val REQUIRED_CUCUMBER_PROPERTIES = listOf(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME)

    private val OPTIONAL_CUCUMBER_PROPERTIES = listOf(
        EXECUTION_DRY_RUN_PROPERTY_NAME,
        EXECUTION_ORDER_PROPERTY_NAME,
        FILTER_NAME_PROPERTY_NAME,
        FILTER_TAGS_PROPERTY_NAME
    )

    private val propertyLoader = PropertyLoader()
    private var initialized = AtomicBoolean(false)


    @JvmStatic
    fun initializeProperties(): JUnitPropertyInitializer {
        if (!initialized.get()) {
            synchronized(initialized) {

                val env = propertyLoader.loadProperties()

                applyHardCodedProperties()
                applySystemProperties(env)
                applyGlueAppendProperty(env)
                applyParallelExecutorConfig(env)

                initialized.set(true)
            }
        }
        return this
    }

    private fun applyHardCodedProperties() {
        System.setProperty(PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, false.toString())
        System.setProperty(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, true.toString())
        System.setProperty(GLUE_PROPERTY_NAME, FRAMEWORK_BOOTSTRAP_GLUE)
    }


    private fun applyParallelExecutorConfig(env: Environment) {
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

    private fun applyExecutionGroupConfig(env: Environment) {
        env.getProperty(PROPERTY_PARALLEL_ISOLATION_TAG)?.let {
            val propertyKey = getExecutionGroupPropertyKey(it)
            System.setProperty(propertyKey, "org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_KEY")
        }

        env.getProperty(PROPERTY_PARALLEL_EXECUTION_GROUP_TAGS)?.let {
            it.split(",").forEach { tag ->
                val propertyKey = getExecutionGroupPropertyKey(tag)
                System.setProperty(propertyKey, tag)
            }
        }
    }

    private fun getExecutionGroupPropertyKey(tag: String): String {
        return EXECUTION_EXCLUSIVE_RESOURCES_READ_WRITE_TEMPLATE.replace(EXECUTION_EXCLUSIVE_RESOURCES_TAG_TEMPLATE_VARIABLE, tag)
    }

    private fun applySystemProperties(env: Environment) {
        REQUIRED_CUCUMBER_PROPERTIES.forEach { cucumberProperty ->
            val propertyKey = PROPERTY_PREFIX + cucumberProperty
            val property = env.getProperty(propertyKey)


            System.setProperty(
                cucumberProperty,
                property ?: throw TestAutomationException("Missing required property [$propertyKey] in application-<$ALL_SPRING_PROFILES>.yml")
            )
        }

        OPTIONAL_CUCUMBER_PROPERTIES.forEach { cucumberProperty ->
            val propertyKey = PROPERTY_PREFIX + cucumberProperty
            val property = env.getProperty(propertyKey)

            property?.let { System.setProperty(cucumberProperty, it) }
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

}