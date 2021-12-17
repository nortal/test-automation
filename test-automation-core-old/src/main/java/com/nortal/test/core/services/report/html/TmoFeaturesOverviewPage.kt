package com.nortal.test.core.services.report.html

import com.nortal.test.core.services.report.cucumber.SkippedScenarios
import net.masterthought.cucumber.Configuration
import net.masterthought.cucumber.ReportResult
import net.masterthought.cucumber.generators.FeaturesOverviewPage

class TmoFeaturesOverviewPage(
    reportResult: ReportResult?, configuration: Configuration?,
    private val skippedScenarios: SkippedScenarios,
    private val gdChanges: ChangedFilesReport
) : FeaturesOverviewPage(reportResult, configuration) {
    override fun prepareReport() {
        super.prepareReport()
        context.put("skippedScenarios", skippedScenarios)
        context.put("gdChanges", gdChanges)
    }
}