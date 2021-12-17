package com.nortal.test.core.testng

import io.cucumber.core.backend.ObjectFactory
import io.cucumber.core.exception.CucumberException
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry

/**
 * This shares a single spring application context with all threads in the test execution jvm. Default cucumber currently (7/3/2019) creates separate
 * context for each thread and its not working well.
 *
 * @author VJose6
 */
class SingleExistingContextSpringFactory : ObjectFactory {
    private val stepClasses: MutableCollection<Class<*>> = HashSet()
    override fun addClass(stepClass: Class<*>): Boolean {
        if (!stepClasses.contains(stepClass)) {
            stepClasses.add(stepClass)
        }
        return true
    }

    override fun start() {
        notifyContextManagerAboutTestClassStarted()
        val beanFactory = CONTEXT_HOLDER!!.applicationContext!!.beanFactory
        for (stepClass in stepClasses) {
            registerStepClassBeanDefinition(beanFactory, stepClass)
        }
    }

    private fun notifyContextManagerAboutTestClassStarted() {
        try {
            CONTEXT_HOLDER!!.beforeTestClass()
        } catch (e: Exception) {
            throw CucumberException(e.message, e)
        }
    }

    private fun registerStepClassBeanDefinition(beanFactory: ConfigurableListableBeanFactory, stepClass: Class<*>) {
        synchronized(CONTEXT_HOLDER!!) {
            val registry = beanFactory as BeanDefinitionRegistry
            val beanDefinition: BeanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(stepClass)
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .beanDefinition
            if (registry.containsBeanDefinition(stepClass.name)) {
                LOGGER.debug("Step class {} was already registered. This is a workaround for shared spring context. Ignoring..", stepClass.name)
            } else {
                registry.registerBeanDefinition(stepClass.name, beanDefinition)
            }
        }
    }

    override fun <T> getInstance(type: Class<T>): T {
        synchronized(CONTEXT_HOLDER!!) {
            return try {
                CONTEXT_HOLDER!!.applicationContext!!.getBean(type)
            } catch (e: BeansException) {
                throw CucumberException(e.message, e)
            }
        }
    }

    override fun stop() {
        notifyContextManagerAboutTestClassFinished()
    }

    private fun notifyContextManagerAboutTestClassFinished() {
        try {
            CONTEXT_HOLDER!!.afterTestClass()
        } catch (e: Exception) {
            throw CucumberException(e.message, e)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SingleExistingContextSpringFactory::class.java)
        @JvmField
        var CONTEXT_HOLDER: SpringContextHolder? = null
    }
}