package com.nortal.test.report.allure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.report.allure")
data class AllureReportProperties(
    /**
     * Report dir where generated report will be put in.
     */
    val reportDir: String = "build/allure-report",
    /**
     * Cucumber result dir in raw allure format.
     */
    val resultDir: String = "build/allure-results",
    @NestedConfigurationProperty
    val serveReport: AllureServeReportProperties = AllureServeReportProperties()
)

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.report.allure.serve-report")
class AllureServeReportProperties(
    /**
     * Serve latest report through embedded webserver.
     */
    val enabled: Boolean = false,
    /**
     * Hostname to use.
     */
    val hostname: String = "127.0.0.1",
    /**
     * Port.
     */
    var port: Int = 9898,
)