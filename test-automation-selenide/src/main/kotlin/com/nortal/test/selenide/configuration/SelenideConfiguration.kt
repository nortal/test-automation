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

import com.codeborne.selenide.logevents.SelenideLogger
import io.qameta.allure.selenide.AllureSelenide
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


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
        System.setProperty("chromeoptions.args", selenideProperties.chromeOptionsArgs)

        log.info("Setting up Selenide with config: {}", selenideProperties)

        com.codeborne.selenide.Configuration.baseUrl = selenideProperties.baseUrl
        com.codeborne.selenide.Configuration.timeout = selenideProperties.timeout
        com.codeborne.selenide.Configuration.pollingInterval = selenideProperties.pollingInterval
        com.codeborne.selenide.Configuration.reopenBrowserOnFail = selenideProperties.reopenBrowserOnFail
        com.codeborne.selenide.Configuration.browser = selenideProperties.browser.name
        com.codeborne.selenide.Configuration.browserVersion = selenideProperties.browserVersion

        com.codeborne.selenide.Configuration.remote = selenideProperties.remote
        com.codeborne.selenide.Configuration.browserSize = selenideProperties.browserSize
        com.codeborne.selenide.Configuration.browserPosition = selenideProperties.browserPosition
        com.codeborne.selenide.Configuration.pageLoadStrategy = selenideProperties.pageLoadStrategy
        com.codeborne.selenide.Configuration.pageLoadTimeout = selenideProperties.pageLoadTimeout
        com.codeborne.selenide.Configuration.clickViaJs = selenideProperties.clickViaJs
        com.codeborne.selenide.Configuration.screenshots = selenideProperties.screenshots
        com.codeborne.selenide.Configuration.savePageSource = selenideProperties.savePageSource
        com.codeborne.selenide.Configuration.reportsFolder = selenideProperties.reportsFolder
        com.codeborne.selenide.Configuration.downloadsFolder = selenideProperties.downloadsFolder
        com.codeborne.selenide.Configuration.fastSetValue = selenideProperties.fastSetValue
        com.codeborne.selenide.Configuration.selectorMode = selenideProperties.selectorMode
        com.codeborne.selenide.Configuration.assertionMode = selenideProperties.assertionMode
        com.codeborne.selenide.Configuration.fileDownload = selenideProperties.fileDownload
        com.codeborne.selenide.Configuration.proxyEnabled = selenideProperties.proxyEnabled
        com.codeborne.selenide.Configuration.proxyHost = selenideProperties.proxyHost
        com.codeborne.selenide.Configuration.proxyPort = selenideProperties.proxyPort
        com.codeborne.selenide.Configuration.webdriverLogsEnabled = selenideProperties.webdriverLogsEnabled
        com.codeborne.selenide.Configuration.headless = selenideProperties.headless
        com.codeborne.selenide.Configuration.browserBinary = selenideProperties.browserBinary

        //TODO: make this optional.
        SelenideLogger.addListener(
            "AllureSelenide", AllureSelenide()
                .screenshots(true)
                .savePageSource(true)
        )
    }
}