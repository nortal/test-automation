package com.nortal.test.report.allure.hook

import com.nortal.test.core.services.hooks.AfterSuiteHook
import com.nortal.test.report.allure.configuration.AllureReportProperties
import io.qameta.allure.Commands
import io.qameta.allure.option.ConfigOptions
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class AllureReportGenerationHook(private val allureReportProperties: AllureReportProperties) : AfterSuiteHook {
    private val commands = Commands(null as Path?)

    override fun afterSuite() {
        val inputDir = Path.of(allureReportProperties.reportDir)
        val outputDirs = listOf(Path.of(allureReportProperties.resultDir))

        commands.generate(inputDir, outputDirs, true, ConfigOptions())

        tryServeReport(outputDirs)
    }

    private fun tryServeReport(outputDirs: List<Path>) {
        if (allureReportProperties.serveReport.enabled) {
            commands.serve(
                outputDirs,
                allureReportProperties.serveReport.hostname,
                allureReportProperties.serveReport.port,
                ConfigOptions()
            )
        }
    }
}