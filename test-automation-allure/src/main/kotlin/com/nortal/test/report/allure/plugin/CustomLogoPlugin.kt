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
package com.nortal.test.report.allure.plugin

import com.nortal.test.core.exception.TestAutomationException
import io.qameta.allure.Aggregator
import io.qameta.allure.core.Configuration
import io.qameta.allure.core.LaunchResults
import java.io.File
import java.nio.file.Path

/**
 * This plugin enables changing logo on the report into a custom one.
 *
 * New logos are controlled by properties `test-automation.report.allure.custom-logo-dir` and
 * `test-automation.report.allure.custom-collapsed-logo-dir`. If `custom-collapsed-logo-dir` is not defined,
 * logo from `custom-logo-dir` will be used with both expanded and collapsed navigation.
 *
 */
class CustomLogoPlugin : Aggregator {

    override fun aggregate(configuration: Configuration?, launchesResults: List<LaunchResults>, outputDirectory: Path) {
        val fullLogoProperty = System.getProperty("allure.custom-logo-dir") ?: return
        val collapsedLogoProperty = System.getProperty("allure.custom-collapsed-logo-dir") ?: fullLogoProperty

        val newFullLogoPath = this.javaClass.getResource(fullLogoProperty) ?:
            throw TestAutomationException("Unable to find custom logo in resource folder path $fullLogoProperty")
        val newCollapsedLogoPath = this.javaClass.getResource(collapsedLogoProperty) ?:
            throw TestAutomationException("Unable to find custom logo in resource folder path $collapsedLogoProperty")

        val newFullLogo = File(newFullLogoPath.toURI())
        val newCollapsedLogo = File(newCollapsedLogoPath.toURI())
        val defaultFullLogo = File(outputDirectory.resolve("plugins/custom-logo/custom-logo.svg").toUri())
        val defaultCollapsedLogo = File(outputDirectory.resolve("plugins/custom-logo/custom-logo-collapsed.svg").toUri())

        newFullLogo.copyTo(defaultFullLogo, true)
        newCollapsedLogo.copyTo(defaultCollapsedLogo, true)
    }
}