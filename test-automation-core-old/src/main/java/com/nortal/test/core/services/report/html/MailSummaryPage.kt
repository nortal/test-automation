package com.nortal.test.core.services.report.html

import com.nortal.test.core.services.report.cucumber.SkippedScenarios
import net.masterthought.cucumber.Configuration
import net.masterthought.cucumber.ReportResult
import net.masterthought.cucumber.generators.AbstractPage
import net.masterthought.cucumber.json.support.TagObject
import java.util.stream.Collectors

class MailSummaryPage(
    reportResult: ReportResult?, configuration: Configuration?, private val reportBaseUrl: String,
    private val capabilities: List<String>,
    private val skippedScenarios: SkippedScenarios,
    private val gdChanges: ChangedFilesReport
) : AbstractPage(reportResult, "mailSummary.vm", configuration) {
    override fun getWebPage(): String {
        return WEB_PAGE
    }

    public override fun prepareReport() {
        context.put("all_features", reportResult.allFeatures)
        context.put("report_summary", reportResult.featureReport)
        context.put("classifications", configuration.classifications)
        context.put("absolutePrefix", reportBaseUrl)
        context.put("capabilityTags", capabilitiesTags)
        context.put("skippedScenarios", skippedScenarios)
        context.put("gdChanges", gdChanges)
    }

    private val capabilitiesTags: List<TagObject>
        private get() = reportResult.allTags.stream()
            .filter { tag: TagObject -> capabilities.contains(tag.name) }
            .collect(Collectors.toList())

    companion object {
        const val WEB_PAGE = "mail-summary.html"
    }
}