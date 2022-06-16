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
package com.nortal.test.selenide.configuration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/**
 * Selenide module configuration.
 */
@Configuration
@EnableConfigurationProperties(
    SelenideProperties::class
)
open class SelenideConfiguration(private val selenideProperties: SelenideProperties) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Selenide instance configuration. Note: configuration is not thread-safe, but execution is.
     */
    @PostConstruct
    open fun initSelenide() {
        log.info("Setting up Selenide with config: {}", selenideProperties)

        com.codeborne.selenide.Configuration.browser = selenideProperties.browser.name
        com.codeborne.selenide.Configuration.browserSize = selenideProperties.browserSize
        com.codeborne.selenide.Configuration.driverManagerEnabled = selenideProperties.driverManagerEnabled

        com.codeborne.selenide.Configuration.headless = selenideProperties.headless
        com.codeborne.selenide.Configuration.timeout = selenideProperties.timeout
        com.codeborne.selenide.Configuration.pageLoadTimeout = selenideProperties.pageLoadTimeout
        com.codeborne.selenide.Configuration.holdBrowserOpen = selenideProperties.holdBrowserOpen
        com.codeborne.selenide.Configuration.screenshots = selenideProperties.screenshotOnFailure

        if (selenideProperties.remoteGridEnabled) {
            com.codeborne.selenide.Configuration.remote = selenideProperties.remoteGridUrl
        }
    }
}