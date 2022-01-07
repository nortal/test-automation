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