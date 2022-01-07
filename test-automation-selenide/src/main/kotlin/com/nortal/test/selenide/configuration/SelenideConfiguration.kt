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