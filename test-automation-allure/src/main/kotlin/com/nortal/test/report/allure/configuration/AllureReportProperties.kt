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
package com.nortal.test.report.allure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "test-automation.report.allure")
data class AllureReportProperties(
    /**
     * Report base dir.
     */
    val baseDir: String,
    /**
     * Report dir where generated report will be put in.
     */
    val reportDir: String,
    /**
     * Cucumber result dir in raw allure format.
     */
    val resultDir: String,
    /**
     * Allure serve report configuration.
     */
    @NestedConfigurationProperty
    val serveReport: AllureServeReportProperties = AllureServeReportProperties(),
    /**
     * Configuration for zipping generated report.
     */
    @NestedConfigurationProperty
    val zipReport: AllureZipReportProperties = AllureZipReportProperties(),
    /**
     * Path to custom logo.
     */
    val customLogo: String? = null,
    /**
     * Path to collapsed custom logo.
     */
    val customCollapsedLogo: String? = null,
)

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

@ConfigurationProperties(prefix = "test-automation.report.allure.zip-report")
class AllureZipReportProperties(
    /**
     * Zip the generated report.
     * The default value is false.
     */
    val enabled: Boolean = false,
    /**
     * Directory where zipped report will be stored.
     * The default directory is set to build.
     */
    val zipDir: String = "build",
)
