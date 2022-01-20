package com.nortal.test.report.allure.hook

import com.nortal.test.core.exception.TestAutomationException
import com.nortal.test.core.report.ReportPublisher
import com.nortal.test.core.services.hooks.AfterSuiteHook
import com.nortal.test.report.allure.configuration.AllureReportProperties
import io.qameta.allure.Commands
import io.qameta.allure.option.ConfigOptions
import org.apache.commons.io.function.IOConsumer
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
    private val reportPublisher: ReportPublisher
) : AfterSuiteHook {
    private val entryFileName = "index.html"

    override fun afterSuite() {
        val reportDir = Path.of(allureReportProperties.reportDir)
        val executionResultDir = Path.of(allureReportProperties.resultDir)

        val resource = this.javaClass.getResource("/allure_home")
            ?: throw TestAutomationException("Missing allure_home under classpath!")

        processResource(resource.toURI()) { allureHome ->
            val commands = Commands(allureHome)
            commands.generate(reportDir, listOf(executionResultDir), true, ConfigOptions())

            reportPublisher.publish(reportDir, entryFileName)
            tryServeReport(commands, executionResultDir)
        }
    }


    private fun tryServeReport(commands: Commands, outputDir: Path) {
        if (allureReportProperties.serveReport.enabled) {
            commands.serve(
                listOf(outputDir),
                allureReportProperties.serveReport.hostname,
                allureReportProperties.serveReport.port,
                ConfigOptions()
            )
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