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
package com.nortal.test.report.allure.hook

import com.nortal.test.core.exception.TestAutomationException
import com.nortal.test.core.report.ReportPublisher
import com.nortal.test.core.services.hooks.AfterSuiteHook
import com.nortal.test.report.allure.configuration.AllureReportProperties
import com.nortal.test.report.allure.services.AllureReportZipService
import io.qameta.allure.Commands
import io.qameta.allure.option.ConfigOptions
import io.qameta.allure.option.ReportLanguageOptions
import io.qameta.allure.option.ReportNameOptions
import org.apache.commons.io.function.IOConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Generate Allure report once test execution finishes.
 */
@Component
class AllureReportGenerationHook(
    private val allureReportProperties: AllureReportProperties,
    private val reportPublisher: ReportPublisher,
    private val allureReportZipService: AllureReportZipService
) : AfterSuiteHook {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val entryFileName = "index.html"

    override fun afterSuitOrder(): Int {
        return 50000
    }

    override fun afterSuite() {
        log.info("Generating Allure Report..")
        val reportDir = FileSystems.getDefault().getPath(allureReportProperties.reportDir)
        val executionResultDir = FileSystems.getDefault().getPath(allureReportProperties.resultDir)

        val resource = this.javaClass.getResource("/allure_home")
            ?: throw TestAutomationException("Missing allure_home under classpath!")

        processResource(resource.toURI()) { allureHome ->
            val commands = Commands(allureHome)
            commands.generate(
                reportDir,
                listOf(executionResultDir),
                true,
                false,
                ConfigOptions(),
                ReportNameOptions(),
                ReportLanguageOptions()
            )

            reportPublisher.publish(reportDir, entryFileName)
            tryServeReport(commands, executionResultDir)
            tryZipReport(reportDir)
        }
    }


    private fun tryServeReport(commands: Commands, outputDir: Path) {
        if (allureReportProperties.serveReport.enabled) {
            commands.serve(
                listOf(outputDir),
                allureReportProperties.serveReport.hostname,
                allureReportProperties.serveReport.port,
                ConfigOptions(),
                ReportNameOptions(),
                ReportLanguageOptions()
            )
        }
    }


    private fun tryZipReport(reportDir: Path) {
        if (allureReportProperties.zipReport.enabled) {
            val zipOutputDir = allureReportProperties.zipReport.zipDir
            allureReportZipService.zipReport(reportDir.toString(), zipOutputDir)
        }
    }


    @Suppress("SwallowedException")
    @Throws(IOException::class)
    fun processResource(uri: URI, action: IOConsumer<Path?>) {
        try {
            val p: Path = Paths.get(uri)
            action.accept(p)
        } catch (ex: FileSystemNotFoundException) {
            FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fs ->
                val p = fs.provider().getPath(uri)
                action.accept(p)
            }
        }
    }

}