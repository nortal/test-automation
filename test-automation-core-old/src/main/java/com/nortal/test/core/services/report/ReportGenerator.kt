package com.nortal.test.core.services.report

import com.nortal.test.core.configuration.ReportProperties
import com.nortal.test.core.services.report.ScenarioSkipService.skippedScenarios
import com.nortal.test.core.services.report.html.ReportBuilder.generateReports
import com.nortal.test.core.services.report.ReportUploader.upload
import com.nortal.test.core.services.report.ReportMailSender.mail
import com.nortal.test.core.services.report.html.TmoReportBuilder
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import net.masterthought.cucumber.Configuration
import net.masterthought.cucumber.json.support.Status
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.lang.AssertionError
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * This class is responsible for generating an html report after the test suite is done running.
 */
@Component
@Slf4j
@RequiredArgsConstructor
class ReportGenerator {
    private val reportProperties: ReportProperties? = null
    private val uploader: ReportUploader? = null
    private val mailer: ReportMailSender? = null
    private val historyService: FileHistoryService? = null
    private val skipService: ScenarioSkipService? = null

    //	private final PostmanAutomationCollectionGenerator postmanAutomationCollectionGenerator;
    @Value("\${integration.hosts.report_upload:#{null}}")
    private val uploadHost: String? = null
    fun generate() {
        val reportOutputDirectory = File(reportProperties!!.output)
        val configuration = Configuration(reportOutputDirectory, reportProperties.buildName)
        configuration.notFailingStatuses = setOf(Status.SKIPPED)
        configuration.buildNumber = reportProperties.buildNumber
        TmoReportBuilder(
            allJsons, configuration, baseUrl,
            reportProperties.capabilities,
            skipService!!.skippedScenarios,
            historyService!!.commits
        ).generateReports()
        uploader!!.upload()
        mailer!!.mail()
    }

    //if there is no uploaded report we short circuit all the links
    private val baseUrl: String
        private get() = if (reportProperties!!.shouldReport()) {
            String.format(
                "%s/v2/testsupport/automation/reports/%s/%s", uploadHost, reportProperties.buildName,
                reportProperties.buildNumber
            )
        } else "data:text/plain;base64,T29wcyEgTG9va3MgbGlrZSB0aGlzIHJlcG9ydCB3YXMgZ2VuZXJhdGVkIGZyb20gYSBsb2NhbCBydW4gYW5kIHdhcyBub3QgdXBsb2FkZWQgdG8gdGhlIHNlcnZlciBmb3Igc3RvcmFnZS4gVGhpcyBtZWFucyB0aGF0IGFsbCBleHRlcm5hbCBmYWNpbmcgbGlua3Mgd2lsbCBiZSBzaG9ydC1jaXJjdWl0ZWQu"

    //if there is no uploaded report we short circuit all the links
    private val allJsons: List<String?>
        private get() = try {
            Files.list(Paths.get(reportProperties!!.output))
                .map { obj: Path -> obj.toString() }
                .filter { path: String? -> path!!.endsWith(JSON_SUFFIX) }
                .collect(Collectors.toList())
        } catch (e: IOException) {
            throw AssertionError("Failed to generate test report due to exception.", e)
        }

    companion object {
        private const val JSON_SUFFIX = ".json"
    }
}