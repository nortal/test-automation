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
package com.nortal.test.testcontainers.images

import com.nortal.test.core.util.SeleniumRemoteProvider
import com.nortal.test.testcontainers.AbstractAuxiliaryContainer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.testcontainers.containers.DefaultGenericContainer

/**
 *Selenium chromium contextual containers. It is intended to be used together with selenide remoting.
 *
 * <p>By default 'seleniarm/standalone-chromium:latest' is default image, but it can be overridden through
 * test-automation.containers.context-containers.selenium-chrome.image-name property.</p>
 */
@Component
@ConditionalOnProperty("test-automation.containers.context-containers.selenium-chrome.enabled", havingValue = "true")
class SeleniumChromeContainer(
    @Value("\${test-automation.containers.context-containers.selenium-chrome.image-name:seleniarm/standalone-chromium:latest}")
    private val imageName: String
) : AbstractAuxiliaryContainer<DefaultGenericContainer>(), SeleniumRemoteProvider {

    companion object {
        private const val PORT = 4444
    }

    override fun configure(): DefaultGenericContainer {
        var container = DefaultGenericContainer(imageName)
        container.addExposedPort(PORT)

        return container
    }

    override fun getConfigurationKey(): String {
        return "selenium-chrome"
    }

    override fun getRemoteUrl(): String {
        return "http://${getTestContainer().host}:${getTestContainer().getMappedPort(PORT)}/wd/hub"
    }
}