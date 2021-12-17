package com.nortal.test.core.testng

import org.springframework.context.ConfigurableApplicationContext
import java.lang.Exception

/**
 * Abstracts the context holder from this package
 *
 * @author VJose6
 */
interface SpringContextHolder {
    val applicationContext: ConfigurableApplicationContext?

    @Throws(Exception::class)
    fun beforeTestClass()

    @Throws(Exception::class)
    fun afterTestClass()
}