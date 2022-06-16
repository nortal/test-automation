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

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Selenide configuration.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.selenide")
data class SelenideProperties(
    /**
     * Browser engines. Default is CHROME.
     */
    val browser: BrowserEngine = BrowserEngine.CHROME,
    /**
     * Browser resolution. Default is 1080p
     */
    val browserSize: String = "1920x1080",
    /**
     * See [com.codeborne.selenide.Configuration.driverManagerEnabled] for details.
     */
    val driverManagerEnabled: Boolean = true,
    /**
     * See [com.codeborne.selenide.Configuration.headless] for details.
     */
    val headless: Boolean = false,
    /**
     * See [com.codeborne.selenide.Configuration.timeout] for details.
     */
    val timeout: Long = 4000,
    /**
     * See [com.codeborne.selenide.Configuration.pageLoadTimeout] for details.
     */
    val pageLoadTimeout: Long = 30000,
    /**
     * See [com.codeborne.selenide.Configuration.holdBrowserOpen] for details.
     */
    val holdBrowserOpen: Boolean = false,
    /**
     * See [com.codeborne.selenide.Configuration.screenshots] for details.
     */
    val screenshotOnFailure: Boolean = true,
    /**
     * Enables remote grid capability. See [com.codeborne.selenide.Configuration.remote] for details.
     */
    val remoteGridEnabled: Boolean = false,
    /**
     * See [com.codeborne.selenide.Configuration.remote] for details.
     */
    val remoteGridUrl: String = "",
)

enum class BrowserEngine {
    CHROME, EDGE, FIREFOX, OPERA, SAFARI
}