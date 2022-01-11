package com.nortal.test.report.allure.hook

import com.nortal.test.core.report.ReportPublisher
import com.nortal.test.core.services.hooks.AfterSuiteHook
import com.nortal.test.report.allure.configuration.AllureReportProperties
import io.qameta.allure.Commands
import io.qameta.allure.option.ConfigOptions
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class AllureReportGenerationHook(
    private val allureReportProperties: AllureReportProperties,
    private val reportPublisher: ReportPublisher
) : AfterSuiteHook {
    private val entryFileName = "index.html"
    private val commands = Commands(null as Path?)

    override fun afterSuite() {
        val reportDir = Path.of(allureReportProperties.reportDir)
        val executionResultDir = Path.of(allureReportProperties.resultDir)

        commands.generate(reportDir, listOf(executionResultDir), true, ConfigOptions())

        reportPublisher.publish(reportDir, entryFileName)
        tryServeReport(executionResultDir)
    }

    private fun tryServeReport(outputDir: Path) {
        if (allureReportProperties.serveReport.enabled) {
            commands.serve(
                listOf(outputDir),
                allureReportProperties.serveReport.hostname,
                allureReportProperties.serveReport.port,
                ConfigOptions()
            )
        }
    }
}